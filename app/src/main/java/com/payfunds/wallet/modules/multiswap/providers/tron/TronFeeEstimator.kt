package com.payfunds.wallet.modules.multiswap.providers.tron

import io.horizontalsystems.tronkit.models.Address
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode

data class SwapQuote(
    val path: List<String>,
    val poolVersion: List<String>,
    val versionLen: List<Long>,
    val fees: List<Long>,
    val amountIn: BigInteger,
    val amountOutMin: BigInteger,
    val to: String,
    val deadline: BigInteger,
    val isTrxInput: Boolean
)

interface TronWallet {
    val addressBase58: String
    fun signTronTx(rawDataHex: String): String
}

data class FeeEstimate(
    val energyRequired: Long,
    val energyPriceSun: Long,
    val energyFeeSun: BigInteger,
    val energyFeeTrx: BigDecimal,
    val suggestedFeeLimitSun: BigInteger
)

class TronFeeEstimator(
    private val nodeUrl: String = "https://api.trongrid.io",
    private val client: OkHttpClient = OkHttpClient()
    ) {

    fun estimateSwapFee(
        wallet: TronWallet,
        quote: SwapQuote,
        safetyFactor: Double = 1.2
    ): Pair<FeeEstimate, JSONObject> {
        val encodedArgs = encodeSwapExactInputArgs(quote)
        val callValue = if (quote.isTrxInput) quote.amountIn else BigInteger.ZERO

        val triggerBody = JSONObject().apply {
            put("owner_address", wallet.addressBase58)
            put("contract_address", ROUTER_ADDRESS_BASE58)
            put("function_selector", FUNCTION_SELECTOR)
            put("parameter", encodedArgs)
            put("call_value", callValue)
            put("fee_limit", 0)
            put("visible", true)
        }

        val triggerResponse = postJson("$nodeUrl/wallet/triggersmartcontract", triggerBody)
        val energyRequired = triggerResponse.optLong("energy_used").takeIf { it > 0 }
            ?: triggerResponse.optLong("energyRequired").takeIf { it > 0 }
            ?: throw IllegalStateException("Missing energy usage in trigger response: $triggerResponse")

        val chainParams = postJson("$nodeUrl/wallet/getchainparameters", JSONObject())
        val energyPriceSun = extractEnergyPrice(chainParams)
            ?: throw IllegalStateException("Missing getEnergyFee in chain parameters: $chainParams")

        val energyFeeSun = BigInteger.valueOf(energyRequired).multiply(BigInteger.valueOf(energyPriceSun))
        val energyFeeTrx = BigDecimal(energyFeeSun)
            .divide(BigDecimal.valueOf(TRX_IN_SUN), 6, RoundingMode.UP)

        val suggestedFeeLimitSun = BigDecimal(energyFeeSun)
            .multiply(BigDecimal.valueOf(safetyFactor))
            .setScale(0, RoundingMode.CEILING)
            .toBigInteger()

        val estimate = FeeEstimate(
            energyRequired = energyRequired,
            energyPriceSun = energyPriceSun,
            energyFeeSun = energyFeeSun,
            energyFeeTrx = energyFeeTrx,
            suggestedFeeLimitSun = suggestedFeeLimitSun
        )

        return estimate to triggerResponse
    }

    private fun encodeSwapExactInputArgs(quote: SwapQuote): String {
        val pathEncoded = encodeAddressArray(quote.path)
        val poolVersionsEncoded = encodeStringArray(quote.poolVersion)
        val versionLenEncoded = encodeUintArray(quote.versionLen.map { BigInteger.valueOf(it) })
        val feesEncoded = encodeUintArray(quote.fees.map { BigInteger.valueOf(it) })
        val swapDataEncoded = encodeSwapDataTuple(
            amountIn = quote.amountIn,
            amountOutMin = quote.amountOutMin,
            to = quote.to,
            deadline = quote.deadline
        )

        // head contains 5 slots: 4 dynamic offsets + static tuple (4 words)
        // 4 dynamic args (path, poolVersion, versionLen, fees) + swapData (4 words)
        val headSize = (32 * 4) + swapDataEncoded.size // 128 bytes for tuple -> dynamic starts at 0x100
        var dynamicOffset = headSize

        val headParts = mutableListOf<ByteArray>()
        headParts += encodeUint(BigInteger.valueOf(dynamicOffset.toLong()))
        dynamicOffset += pathEncoded.size

        headParts += encodeUint(BigInteger.valueOf(dynamicOffset.toLong()))
        dynamicOffset += poolVersionsEncoded.size

        headParts += encodeUint(BigInteger.valueOf(dynamicOffset.toLong()))
        dynamicOffset += versionLenEncoded.size

        headParts += encodeUint(BigInteger.valueOf(dynamicOffset.toLong()))
        dynamicOffset += feesEncoded.size

        headParts += swapDataEncoded

        val buffer = ByteArrayOutputStream()
        headParts.forEach { buffer.write(it) }
        buffer.write(pathEncoded)
        buffer.write(poolVersionsEncoded)
        buffer.write(versionLenEncoded)
        buffer.write(feesEncoded)

        return buffer.toByteArray().toHex()
    }

    private fun encodeSwapDataTuple(
        amountIn: BigInteger,
        amountOutMin: BigInteger,
        to: String,
        deadline: BigInteger
    ): ByteArray {
        val buffer = ByteArrayOutputStream()
        buffer.write(encodeUint(amountIn))
        buffer.write(encodeUint(amountOutMin))
        buffer.write(encodeAddress(to))
        buffer.write(encodeUint(deadline))
        return buffer.toByteArray()
    }

    private fun encodeAddressArray(values: List<String>): ByteArray {
        val buffer = ByteArrayOutputStream()
        buffer.write(encodeUint(BigInteger.valueOf(values.size.toLong())))
        values.forEach { address ->
            buffer.write(encodeAddress(address))
        }
        return buffer.toByteArray()
    }

    private fun encodeUintArray(values: List<BigInteger>): ByteArray {
        val buffer = ByteArrayOutputStream()
        buffer.write(encodeUint(BigInteger.valueOf(values.size.toLong())))
        values.forEach { value -> buffer.write(encodeUint(value)) }
        return buffer.toByteArray()
    }

    private fun encodeStringArray(values: List<String>): ByteArray {
        val heads = mutableListOf<ByteArray>()
        val tails = mutableListOf<ByteArray>()

        var offset = 32 + (32 * values.size) // length word + head area
        values.forEach { value ->
            val encodedString = encodeDynamicString(value)
            heads += encodeUint(BigInteger.valueOf(offset.toLong()))
            tails += encodedString
            offset += encodedString.size
        }

        val buffer = ByteArrayOutputStream()
        buffer.write(encodeUint(BigInteger.valueOf(values.size.toLong())))
        heads.forEach { buffer.write(it) }
        tails.forEach { buffer.write(it) }
        return buffer.toByteArray()
    }

    private fun encodeDynamicString(value: String): ByteArray {
        val data = value.toByteArray(Charsets.UTF_8)
        val buffer = ByteArrayOutputStream()
        buffer.write(encodeUint(BigInteger.valueOf(data.size.toLong())))
        buffer.write(padRightToWord(data))
        return buffer.toByteArray()
    }

    private fun encodeAddress(address: String): ByteArray {
        val hex = Address.fromBase58(address).hex.removePrefix("0x")
        return padLeftToWord(hexStringToByteArray(hex))
    }

    private fun encodeUint(value: BigInteger): ByteArray {
        val normalized = value.toByteArray().let {
            if (it.size > 32) it.copyOfRange(it.size - 32, it.size) else it
        }
        return padLeftToWord(normalized)
    }

    private fun padLeftToWord(data: ByteArray): ByteArray {
        val result = ByteArray(32)
        val copyFrom = data.size.coerceAtMost(32)
        System.arraycopy(data, data.size - copyFrom, result, 32 - copyFrom, copyFrom)
        return result
    }

    private fun padRightToWord(data: ByteArray): ByteArray {
        val paddedSize = ((data.size + 31) / 32) * 32
        val result = ByteArray(paddedSize)
        System.arraycopy(data, 0, result, 0, data.size)
        return result
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

    private fun extractEnergyPrice(chainParams: JSONObject): Long? {
        val params = chainParams.optJSONArray("chainParameter") ?: return null
        return params.findValue("getEnergyFee")?.toLongOrNull()
    }

    private fun JSONArray.findValue(key: String): String? {
        for (i in 0 until length()) {
            val item = optJSONObject(i) ?: continue
            if (item.optString("key") == key) {
                return item.optString("value")
            }
        }
        return null
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun hexStringToByteArray(hex: String): ByteArray {
        var cleanHex = hex.removePrefix("0x")
        if (cleanHex.length % 2 != 0) {
            cleanHex = "0$cleanHex"
        }
        val data = ByteArray(cleanHex.length / 2)
        for (i in data.indices) {
            val index = i * 2
            data[i] = ((cleanHex[index].digitToInt(16) shl 4) + cleanHex[index + 1].digitToInt(16)).toByte()
        }
        return data
    }

    companion object {
        private const val ROUTER_ADDRESS_BASE58 = "TCFNp179Lg46D16zKoumd4Poa2WFFdtqYj"
        private const val FUNCTION_SELECTOR = "swapExactInput(address[],string[],uint256[],uint24[],(uint256,uint256,address,uint256))"
        private const val TRX_IN_SUN = 1_000_000L
    }
}

object TronFeeEstimatorExample {
    @JvmStatic
    fun main(args: Array<String>) {
        val estimator = TronFeeEstimator()

        val quote = SwapQuote(
            path = listOf(
                "T9yD14Nj9j7xAB4dbGeiX9h8unkKHxuWwb", // WTRX
                "TEkxiTehnzSmSe2XqrBj4w32RUN966rdz8"  // USDT
            ),
            poolVersion = listOf("V2"),
            versionLen = listOf(2L),
            fees = listOf(0L, 0L),
            amountIn = BigInteger.valueOf(1_000_000L), // 1 TRX
            amountOutMin = BigInteger.valueOf(900_000L),
            to = "TXYZopYRdj2D9XRtbG411XZZ3kM5VkAeBf",
            deadline = BigInteger.valueOf(System.currentTimeMillis() / 1000 + 600),
            isTrxInput = true
        )

        val wallet = object : TronWallet {
            override val addressBase58: String = "TXYZopYRdj2D9XRtbG411XZZ3kM5VkAeBf"
            override fun signTronTx(rawDataHex: String): String = ""
        }

        val (fee, response) = estimator.estimateSwapFee(wallet, quote)
        println("Energy required: ${fee.energyRequired}")
        println("Energy price (sun): ${fee.energyPriceSun}")
        println("Energy fee (TRX): ${fee.energyFeeTrx}")
        println("Suggested feeLimit (sun): ${fee.suggestedFeeLimitSun}")
        println("Trigger response: $response")
    }
}
