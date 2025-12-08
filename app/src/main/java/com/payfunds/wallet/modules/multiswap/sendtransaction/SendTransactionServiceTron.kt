package com.payfunds.wallet.modules.multiswap.sendtransaction

import android.util.Log
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.ethereum.CautionViewItem
import com.payfunds.wallet.core.managers.TronKitWrapper
import com.payfunds.wallet.entities.CoinValue
import com.payfunds.wallet.entities.Wallet
import com.payfunds.wallet.modules.multiswap.providers.tron.TronGridServiceNew
import com.payfunds.wallet.modules.multiswap.ui.DataField
import com.payfunds.wallet.modules.send.SendModule
import com.payfunds.wallet.entities.CurrencyValue
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.marketkit.models.TokenQuery
import io.horizontalsystems.marketkit.models.TokenType
import io.horizontalsystems.tronkit.contracts.ContractMethodHelper
import io.horizontalsystems.tronkit.contracts.trc20.ApproveMethod
import io.horizontalsystems.tronkit.models.Address
import io.horizontalsystems.tronkit.models.TriggerSmartContract
import io.horizontalsystems.tronkit.network.ApiKeyProvider
import io.horizontalsystems.tronkit.network.Network
import io.horizontalsystems.tronkit.transaction.Fee
import io.horizontalsystems.tronkit.transaction.Signer
import io.horizontalsystems.tronkit.toRawHexString
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import java.math.BigInteger
import java.util.concurrent.ConcurrentHashMap

class SendTransactionServiceTron(
    private val token: Token
) : ISendTransactionService() {

    private val wallet: Wallet = App.walletManager.activeWallets.firstOrNull { it.token == token }
        ?: throw IllegalStateException("Wallet not found for token ${token.coin.code}")

    private val ownerAddress: Address by lazy {
        val wrapper = App.tronKitManager.getTronKitWrapper(wallet.account)
        try {
            wrapper.tronKit.address
        } finally {
            App.tronKitManager.unlink(wallet.account)
        }
    }

    private val baseToken by lazy {
        App.coinManager.getToken(TokenQuery(BlockchainType.Tron, TokenType.Native))
    }

    private val routerAddress = Address.fromBase58(SUNSWAP_ROUTER_BASE58)
    private val tronGridServices = ConcurrentHashMap<Network, TronGridServiceNew>()
    private var needsApproval = false

    private val _sendTransactionSettingsFlow =
        MutableStateFlow<SendTransactionSettings>(SendTransactionSettings.Tron(ownerAddress))
    override val sendTransactionSettingsFlow = _sendTransactionSettingsFlow.asStateFlow()

    private var networkFee: SendModule.AmountData? = null
    private var cautions: List<CautionViewItem> = listOf()
    private var sendable: Boolean = false
    private var loading: Boolean = false
    private var fields: List<DataField> = listOf()
    private var feeLimit: Long? = null
    private var sendTransactionData: SendTransactionData.Tron? = null

    private var scope: CoroutineScope? = null
    private var estimationJob: Job? = null

    override fun createState() = SendTransactionServiceState(
        networkFee = networkFee,
        cautions = cautions,
        sendable = sendable,
        loading = loading,
        fields = fields
    )

    override fun start(coroutineScope: CoroutineScope) {
        scope = coroutineScope
    }

    override fun setSendTransactionData(data: SendTransactionData) {
        require(data is SendTransactionData.Tron)

        sendTransactionData = data
        loading = true
        cautions = emptyList()
        sendable = false
        networkFee = null
        feeLimit = data.feeLimit
        emitState()

        estimationJob?.cancel()
        estimationJob = scope?.launch(Dispatchers.IO) {
            val (baseFeeSun: BigInteger, computedFeeLimit: Long) = try {
                val providedEstimate = data.feeEstimate
                
                if (providedEstimate != null) {
                    // Use actual estimated fee (energyFeeSun) for fee calculation
                    val actualFeeSun = providedEstimate.energyFeeSun
                    
                    // Use suggested fee limit (with safety factor) for the fee limit
                    val providedFeeLimitSun = providedEstimate.suggestedFeeLimitSun
                    val providedFeeLimitLong = providedFeeLimitSun
                        .min(BigInteger.valueOf(Long.MAX_VALUE))
                        .toLong()
                    
                    val feeLimit = listOfNotNull(
                        providedFeeLimitLong,
                        data.feeLimit,
                        MIN_FEE_LIMIT
                    ).maxOrNull() ?: MIN_FEE_LIMIT
                    
                    Pair(actualFeeSun, feeLimit)
                } else {
                    // Fallback: estimate fee using TronKit
                    val fees = useTronKit { it.tronKit.estimateFee(data.contract) }
                    val totalFeeInSun = fees.sumOf(Fee::feeInSuns)
                    val feeLimit = listOfNotNull(
                        totalFeeInSun,
                        data.feeLimit,
                        MIN_FEE_LIMIT
                    ).maxOrNull() ?: MIN_FEE_LIMIT
                    
                    Pair(totalFeeInSun.toBigInteger(), feeLimit)
                }
            } catch (error: Throwable) {
                Log.w("SendTronFee", "Fee estimation failed, falling back to minimum", error)
                Pair(MIN_FEE_LIMIT.toBigInteger(), MIN_FEE_LIMIT)
            }

            feeLimit = computedFeeLimit

            val allowanceRaw = try {
                useTronKit { wrapper -> fetchAllowance(wrapper) }
            } catch (_: Throwable) {
                null
            }
            needsApproval = allowanceRaw?.let { it < data.amountInScaled } ?: false

            val totalFeeSun = baseFeeSun + BigInteger.valueOf(
                if (needsApproval) APPROVE_FEE_LIMIT else 0L
            )

            networkFee = buildNetworkFeeAmount(totalFeeSun)

            val signerAvailable = signerAvailable()
            cautions = buildCautions(signerAvailable = signerAvailable, needsApproval = needsApproval)
            sendable = signerAvailable

            loading = false
            emitState()
        }
    }

    @Composable
    override fun GetSettingsContent(navController: NavController) {
        // Tron swap does not expose additional settings
    }

    override suspend fun sendTransaction(): SendTransactionResult {
        val data = sendTransactionData ?: throw IllegalStateException("Send data not set")
        val feeLimitToUse = feeLimit ?: MIN_FEE_LIMIT

        return useTronKit { wrapper ->
            val signer = wrapper.signer ?: throw IllegalStateException("Account is watch-only")
            // Always approve first for TRC-20 input to avoid swap rejection
            if (token.type is TokenType.Eip20) {
                performApproval(wrapper, signer, data.amountInScaled)
                needsApproval = false
            }
            val tronKit = wrapper.tronKit
            val txId = tronKit.send(data.contract, signer, feeLimitToUse)

            SendTransactionResult.Tron(
                txId = txId,
                fromAddress = tronKit.address.base58,
                toAddress = data.contract.contractAddress.base58,
                feeCoinCode = baseToken?.coin?.code ?: "TRX"
            )
        }
    }

    private fun buildNetworkFeeAmount(feeInSun: BigInteger?): SendModule.AmountData? {
        val feeLimitSun = feeInSun ?: return null
        val feeToken = baseToken ?: return null

        val value = feeLimitSun.toBigDecimal().movePointLeft(feeToken.decimals)
            .setScale(feeToken.decimals, RoundingMode.DOWN)
            .stripTrailingZeros()

        val coinValue = CoinValue(feeToken.coin, feeToken.decimals, value)
        val primary = SendModule.AmountInfo.CoinValueInfo(coinValue, approximate = true)

        // Calculate USD value for the fee
        val baseCurrency = App.currencyManager.baseCurrency
        val coinPrice = App.marketKit.coinPrice(feeToken.coin.uid, baseCurrency.code)
        val secondary = coinPrice?.let { price ->
            val fiatValue = value.multiply(price.value)
                .setScale(baseCurrency.decimal, RoundingMode.DOWN)
                .stripTrailingZeros()
            SendModule.AmountInfo.CurrencyValueInfo(
                CurrencyValue(baseCurrency, fiatValue),
                approximate = true
            )
        }

        return SendModule.AmountData(primary, secondary)
    }

    private fun buildCautions(
        signerAvailable: Boolean,
        needsApproval: Boolean
    ): List<CautionViewItem> {
        val items = mutableListOf<CautionViewItem>()

        if (!signerAvailable) {
            val message = App.instance.getString(R.string.Tron_WatchAccount_NotSupported)
            items += CautionViewItem(message, "", CautionViewItem.Type.Error)
            return items
        }

        if (needsApproval) {
            val message = App.instance.getString(R.string.Swap_Tron_ApprovalRequired)
            items += CautionViewItem(message, "", CautionViewItem.Type.Warning)
        }

        return items
    }

    private suspend fun signerAvailable(): Boolean {
        return useTronKit { it.signer != null }
    }

    private fun tronGridService(network: Network): TronGridServiceNew {
        return tronGridServices.getOrPut(network) {
            TronGridServiceNew(
                network,
                ApiKeyProvider(App.appConfigProvider.trongridApiKeys)
            )
        }
    }

    private suspend fun fetchAllowance(wrapper: TronKitWrapper): BigInteger? {
        val tokenType = token.type as? TokenType.Eip20 ?: return null
        val contractAddress = parseAddress(tokenType.address)

        val methodId = ContractMethodHelper.getMethodId(METHOD_ALLOWANCE)
        val data = ContractMethodHelper
            .encodedABI(methodId, listOf(wrapper.tronKit.address, routerAddress))
            .toRawHexString()
        val response = tronGridService(wrapper.tronKit.network)
            .ethCall(ensureHexPrefix(contractAddress.hex), ensureHexPrefix(data))
        if (response.isEmpty()) return BigInteger.ZERO

        val bytes = if (response.size >= 32) {
            response.copyOfRange(response.size - 32, response.size)
        } else {
            response
        }
        return BigInteger(1, bytes)
    }

    private suspend fun performApproval(
        wrapper: TronKitWrapper,
        signer: Signer,
        amount: BigInteger
    ) {
        val tokenType = token.type as? TokenType.Eip20
            ?: throw IllegalStateException("Unsupported token type for approval")
        val contractAddress = parseAddress(tokenType.address)
        val method = ApproveMethod(routerAddress, amount)
        val data = method.encodedABI().toRawHexString()
        val parameter = ContractMethodHelper
            .encodedABI(methodId = byteArrayOf(), arguments = method.getArguments())
            .toRawHexString()
        val contract = TriggerSmartContract(
            data = data,
            ownerAddress = wrapper.tronKit.address,
            contractAddress = contractAddress,
            callValue = BigInteger.ZERO,
            callTokenValue = null,
            tokenId = null,
            functionSelector = ApproveMethod.methodSignature,
            parameter = parameter
        )
        wrapper.tronKit.send(contract, signer, APPROVE_FEE_LIMIT)
    }

    private suspend fun <T> useTronKit(block: suspend (TronKitWrapper) -> T): T {
        val wrapper = App.tronKitManager.getTronKitWrapper(wallet.account)
        return try {
            block(wrapper)
        } finally {
            App.tronKitManager.unlink(wallet.account)
        }
    }

    private fun parseAddress(raw: String): Address {
        return try {
            Address.fromBase58(raw)
        } catch (_: Exception) {
            val normalized = raw.removePrefix("0x")
            val withPrefix = if (normalized.startsWith("41", true)) normalized else "41$normalized"
            Address.fromHex(withPrefix)
        }
    }

    private fun ensureHexPrefix(value: String): String {
        return if (value.startsWith("0x", true)) value else "0x$value"
    }

    companion object {
        private const val MIN_FEE_LIMIT = 30_000_000L
        private const val APPROVE_FEE_LIMIT = 30_000_000L
        private const val SUNSWAP_ROUTER_BASE58 = "TCFNp179Lg46D16zKoumd4Poa2WFFdtqYj"
        private const val METHOD_ALLOWANCE = "allowance(address,address)"
    }
}
