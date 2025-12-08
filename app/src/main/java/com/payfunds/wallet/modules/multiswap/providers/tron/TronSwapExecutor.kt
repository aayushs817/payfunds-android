package com.payfunds.wallet.modules.multiswap.providers.tron

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.horizontalsystems.tronkit.models.Address
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.math.BigInteger
import kotlin.math.min

class TronSwapExecutor(
    private val nodeUrl: String = "https://api.trongrid.io",
    private val client: OkHttpClient = OkHttpClient()
) {

    fun executeSwap(
        wallet: TronWallet,
        quote: SwapQuote,
        triggerResponse: JSONObject,
        feeEstimate: FeeEstimate
    ): String {
        return if (quote.isTrxInput) {
            broadcastSwap(wallet, triggerResponse, feeEstimate)
        } else {
            // TRC-20 input requires approval first
            val approveTxId = approveIfNeeded(wallet, quote)
            if (approveTxId.isBlank()) {
                throw IllegalStateException("Approve transaction failed")
            }
            waitForConfirmation(approveTxId)
            broadcastSwap(wallet, triggerResponse, feeEstimate)
        }
    }

    private fun approveIfNeeded(wallet: TronWallet, quote: SwapQuote): String {
        val tokenAddress = quote.path.firstOrNull()
            ?: throw IllegalStateException("Missing token path for approval")

        val parameter = encodeApproveParams(
            spenderBase58 = ROUTER_ADDRESS_BASE58,
            amount = quote.amountIn
        )

        val body = JSONObject().apply {
            put("owner_address", wallet.addressBase58)
            put("contract_address", tokenAddress)
            put("function_selector", "approve(address,uint256)")
            put("parameter", parameter)
            put("call_value", 0)
            put("fee_limit", APPROVE_FEE_LIMIT_SUN)
            put("visible", true)
        }

        val trigger = postJson("$nodeUrl/wallet/triggersmartcontract", body)
        val tx = trigger.optJSONObject("transaction")
            ?: throw IllegalStateException("Approve: missing transaction in response $trigger")

        tx.put("fee_limit", APPROVE_FEE_LIMIT_SUN)
        val signed = signTransaction(wallet, tx)
        val broadcast = postJson("$nodeUrl/wallet/broadcasttransaction", signed)
        if (!broadcast.optBoolean("result")) {
            val message = broadcast.optString("message")
            throw IllegalStateException("Approve broadcast failed: $message")
        }
        return broadcast.optString("txid")
    }

    private fun broadcastSwap(
        wallet: TronWallet,
        triggerResponse: JSONObject,
        feeEstimate: FeeEstimate
    ): String {
        val tx = triggerResponse.optJSONObject("transaction")
            ?: throw IllegalStateException("Swap: missing transaction in trigger response $triggerResponse")

        val feeLimit = feeEstimate.suggestedFeeLimitSun
            .max(BigInteger.valueOf(MIN_FEE_LIMIT_SUN))
            .min(BigInteger.valueOf(Long.MAX_VALUE))
            .toLong()

        tx.put("fee_limit", feeLimit)

        val signed = signTransaction(wallet, tx)
        val broadcast = postJson("$nodeUrl/wallet/broadcasttransaction", signed)
        if (!broadcast.optBoolean("result")) {
            val message = broadcast.optString("message")
            throw IllegalStateException("Swap broadcast failed: $message")
        }
        return broadcast.optString("txid")
    }

    private fun signTransaction(wallet: TronWallet, tx: JSONObject): JSONObject {
        val rawHex = tx.optString("raw_data_hex")
        if (rawHex.isNullOrBlank()) {
            throw IllegalStateException("Missing raw_data_hex for signing")
        }
        val signature = wallet.signTronTx(rawHex)
        tx.put("signature", listOf(signature))
        return tx
    }

    private fun encodeApproveParams(spenderBase58: String, amount: BigInteger): String {
        val spenderHex = Address.fromBase58(spenderBase58).hex.removePrefix("0x")
        val normalizedSpender = spenderHex.removePrefix("41")
        val spenderBytes = hexStringToBytes(normalizedSpender)
        val spenderPadded = padLeftToWord(spenderBytes)
        val amountPadded = padLeftToWord(amount.toByteArray())

        val buffer = ByteArrayOutputStream()
        buffer.write(spenderPadded)
        buffer.write(amountPadded)
        return buffer.toByteArray().toHex()
    }

    private fun postJson(url: String, payload: JSONObject): JSONObject {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = payload.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).execute().use { response ->
            val responseBody = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                throw IllegalStateException("HTTP ${response.code} from $url: $responseBody")
            }
            if (responseBody.isBlank()) {
                throw IllegalStateException("Empty response from $url")
            }
            return JSONObject(responseBody)
        }
    }

    private fun padLeftToWord(data: ByteArray): ByteArray {
        val trimmed = if (data.size > 32) data.copyOfRange(data.size - 32, data.size) else data
        val result = ByteArray(32)
        val copyFrom = trimmed.size
        System.arraycopy(trimmed, 0, result, 32 - copyFrom, copyFrom)
        return result
    }

    private fun hexStringToBytes(hex: String): ByteArray {
        var cleanHex = hex
        if (cleanHex.length % 2 != 0) cleanHex = "0$cleanHex"
        val result = ByteArray(cleanHex.length / 2)
        for (i in result.indices) {
            val idx = i * 2
            result[i] = ((cleanHex[idx].digitToInt(16) shl 4) + cleanHex[idx + 1].digitToInt(16)).toByte()
        }
        return result
    }

    private fun waitForConfirmation(
        txId: String,
        timeoutMs: Long = 30_000,
        intervalMs: Long = 1_000
    ) {
        val deadline = System.currentTimeMillis() + timeoutMs
        while (System.currentTimeMillis() < deadline) {
            val info = postJson("$nodeUrl/wallet/gettransactioninfobyid", JSONObject().put("value", txId))
            val receipt = info.optJSONObject("receipt")
            val result = receipt?.optString("result")
            if (result == "SUCCESS") return
            if (result != null && result != "SUCCESS") {
                val message = info.optString("resMessage", result)
                throw IllegalStateException("Transaction $txId failed: $message")
            }
            Thread.sleep(intervalMs)
        }
        throw IllegalStateException("Transaction $txId not confirmed in time")
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    companion object {
        private const val ROUTER_ADDRESS_BASE58 = "TCFNp179Lg46D16zKoumd4Poa2WFFdtqYj"
        private const val MIN_FEE_LIMIT_SUN = 30_000_000L
        private const val APPROVE_FEE_LIMIT_SUN = 30_000_000L
    }
}

sealed class SwapConfirmationState {
    object Idle : SwapConfirmationState()
    object SigningAndBroadcasting : SwapConfirmationState()
    data class Success(val txId: String) : SwapConfirmationState()
    data class Error(val message: String) : SwapConfirmationState()
}

class SwapConfirmationViewModel(
    private val wallet: TronWallet,
    private val quote: SwapQuote,
    private val feeEstimate: FeeEstimate,
    private val triggerResponse: JSONObject,
    private val executor: TronSwapExecutor
) : ViewModel() {

    private val _state = MutableLiveData<SwapConfirmationState>(SwapConfirmationState.Idle)
    val state: LiveData<SwapConfirmationState> = _state

    fun onConfirmSwapClicked() {
        _state.postValue(SwapConfirmationState.SigningAndBroadcasting)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val txId = executor.executeSwap(wallet, quote, triggerResponse, feeEstimate)
                _state.postValue(SwapConfirmationState.Success(txId))
            } catch (t: Throwable) {
                _state.postValue(SwapConfirmationState.Error(t.message ?: "Swap failed"))
            }
        }
    }
}

/*
Example Fragment wiring:

class SwapConfirmationFragment : Fragment() {

    private val viewModel: SwapConfirmationViewModel by viewModels {
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return SwapConfirmationViewModel(
                    wallet = providedWallet,              // real TronWallet
                    quote = providedQuote,                // SwapQuote from previous screen
                    feeEstimate = providedFeeEstimate,    // FeeEstimate from estimator
                    triggerResponse = providedTriggerJson,// JSONObject from estimator
                    executor = TronSwapExecutor()
                ) as T
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.state.observe(viewLifecycleOwner) { state ->
            when (state) {
                is SwapConfirmationState.Idle -> {
                    // show quote/fee, enable confirm
                }
                is SwapConfirmationState.SigningAndBroadcasting -> {
                    // show loading, disable button
                }
                is SwapConfirmationState.Success -> {
                    // show success, navigate to dashboard
                    findNavController().navigate(R.id.dashboardFragment)
                }
                is SwapConfirmationState.Error -> {
                    // show error message
                }
            }
        }

        binding.confirmButton.setOnClickListener {
            viewModel.onConfirmSwapClicked()
        }
    }
}
*/
