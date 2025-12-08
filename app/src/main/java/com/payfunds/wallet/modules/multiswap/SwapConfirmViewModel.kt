package com.payfunds.wallet.modules.multiswap

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.gson.Gson
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.HSCaution
import com.payfunds.wallet.core.ViewModelUiState
import com.payfunds.wallet.core.ethereum.CautionViewItem
import com.payfunds.wallet.core.managers.CurrencyManager
import com.payfunds.wallet.core.stats.StatEvent
import com.payfunds.wallet.core.stats.StatPage
import com.payfunds.wallet.core.stats.stat
import com.payfunds.wallet.entities.Currency
import com.payfunds.wallet.modules.multiswap.providers.IMultiSwapProvider
import com.payfunds.wallet.modules.multiswap.sendtransaction.ISendTransactionService
import com.payfunds.wallet.modules.multiswap.sendtransaction.SendTransactionResult
import com.payfunds.wallet.modules.multiswap.sendtransaction.SendTransactionServiceFactory
import com.payfunds.wallet.modules.multiswap.sendtransaction.SendTransactionSettings
import com.payfunds.wallet.modules.multiswap.ui.DataField
import com.payfunds.wallet.modules.send.SendModule
import com.payfunds.wallet.modules.settings.security.twofactorauth.CrateUserTokenManager
import com.payfunds.wallet.network.PayFundRetrofitInstance
import com.payfunds.wallet.network.request_model.send_transaction_details.SendTransactionDetailsRequestModel
import com.payfunds.wallet.network.response_model.send_transaction_details.SendTransactionDetailsResponseModel
import com.payfunds.wallet.network.response_model.two_factor_auth.error.TwoFAErrorResponseModel
import io.horizontalsystems.binancechainkit.BinanceChainKit.Companion.wallet
import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.marketkit.models.TokenType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.math.BigDecimal
import kotlin.toString

class SwapConfirmViewModel(
    private val swapProvider: IMultiSwapProvider,
    swapQuote: ISwapQuote,
    private val swapSettings: Map<String, Any?>,
    private val currencyManager: CurrencyManager,
    private val fiatServiceIn: FiatService,
    private val fiatServiceOut: FiatService,
    private val fiatServiceOutMin: FiatService,
    val sendTransactionService: ISendTransactionService,
    private val timerService: TimerService,
    private val priceImpactService: PriceImpactService
) : ViewModelUiState<SwapConfirmUiState>() {
    private var sendTransactionSettings: SendTransactionSettings? = null
    private val currency = currencyManager.baseCurrency
    private val tokenIn = swapQuote.tokenIn
    private val tokenOut = swapQuote.tokenOut
    private val amountIn = swapQuote.amountIn
    private var fiatAmountIn: BigDecimal? = null

    private var fiatAmountOut: BigDecimal? = null
    private var fiatAmountOutMin: BigDecimal? = null

    private var loading = true
    private var timerState = timerService.stateFlow.value
    private var sendTransactionState = sendTransactionService.stateFlow.value
    private var priceImpactState = priceImpactService.stateFlow.value

    private var amountOut: BigDecimal? = null
    private var amountOutMin: BigDecimal? = null
    private var quoteFields: List<DataField> = listOf()

    var sendTransactionDetailsResponseModel =
        mutableStateOf<SendTransactionDetailsResponseModel?>(null)

    var isError = mutableStateOf<String?>(null)
    var isLoading = mutableStateOf(false)

    private val tokenManager = CrateUserTokenManager(App.instance)

    init {
        fiatServiceIn.setCurrency(currency)
        fiatServiceIn.setToken(tokenIn)
        fiatServiceIn.setAmount(amountIn)

        fiatServiceOut.setCurrency(currency)
        fiatServiceOut.setToken(tokenOut)
        fiatServiceOut.setAmount(amountOut)

        fiatServiceOutMin.setCurrency(currency)
        fiatServiceOutMin.setToken(tokenOut)
        fiatServiceOutMin.setAmount(amountOutMin)

        viewModelScope.launch {
            fiatServiceIn.stateFlow.collect {
                fiatAmountIn = it.fiatAmount
                emitState()
            }
        }

        viewModelScope.launch {
            fiatServiceOut.stateFlow.collect {
                fiatAmountOut = it.fiatAmount
                emitState()
            }
        }

        viewModelScope.launch {
            fiatServiceOutMin.stateFlow.collect {
                fiatAmountOutMin = it.fiatAmount
                emitState()
            }
        }

        viewModelScope.launch {
            sendTransactionService.sendTransactionSettingsFlow.collect {
                sendTransactionSettings = it

                fetchFinalQuote()
            }
        }

        viewModelScope.launch {
            sendTransactionService.stateFlow.collect { transactionState ->
                sendTransactionState = transactionState

                loading = transactionState.loading

                emitState()

                if (sendTransactionState.sendable) {
                    timerService.start(10)
                }
            }
        }

        viewModelScope.launch {
            timerService.stateFlow.collect {
                timerState = it

                emitState()
            }
        }

        viewModelScope.launch {
            priceImpactService.stateFlow.collect {
                handleUpdatedPriceImpactState(it)
            }
        }

        sendTransactionService.start(viewModelScope)

        fetchFinalQuote()
    }

    private fun handleUpdatedPriceImpactState(priceImpactState: PriceImpactService.State) {
        this.priceImpactState = priceImpactState

        emitState()
    }

    override fun createState(): SwapConfirmUiState {
        var cautions = sendTransactionState.cautions

        if (cautions.isEmpty()) {
            priceImpactState.priceImpactCaution?.let { hsCaution ->
                cautions = listOf(
                    CautionViewItem(
                        hsCaution.s.toString(),
                        hsCaution.description.toString(),
                        when (hsCaution.type) {
                            HSCaution.Type.Error -> CautionViewItem.Type.Error
                            HSCaution.Type.Warning -> CautionViewItem.Type.Warning
                        }
                    )
                )
            }
        }

        return SwapConfirmUiState(
            expiresIn = timerState.remaining,
            expired = timerState.timeout,
            loading = loading,
            tokenIn = tokenIn,
            tokenOut = tokenOut,
            amountIn = amountIn,
            amountOut = amountOut,
            amountOutMin = amountOutMin,
            fiatAmountIn = fiatAmountIn,
            fiatAmountOut = fiatAmountOut,
            fiatAmountOutMin = fiatAmountOutMin,
            currency = currency,
            networkFee = sendTransactionState.networkFee,
            cautions = cautions,
            validQuote = sendTransactionState.sendable,
            priceImpact = priceImpactState.priceImpact,
            priceImpactLevel = priceImpactState.priceImpactLevel,
            quoteFields = quoteFields,
            transactionFields = sendTransactionState.fields,
        )
    }

    override fun onCleared() {
        timerService.stop()
    }

    fun refresh() {
        loading = true
        emitState()

        fetchFinalQuote()

        stat(page = StatPage.SwapConfirmation, event = StatEvent.Refresh)
    }

    private fun fetchFinalQuote() {
        viewModelScope.launch(Dispatchers.Default) {
            try {
                val finalQuote = swapProvider.fetchFinalQuote(
                    tokenIn,
                    tokenOut,
                    amountIn,
                    swapSettings,
                    sendTransactionSettings
                )

                amountOut = finalQuote.amountOut
                amountOutMin = finalQuote.amountOutMin
                quoteFields = finalQuote.fields
                emitState()

                fiatServiceOut.setAmount(amountOut)
                fiatServiceOutMin.setAmount(amountOutMin)
                sendTransactionService.setSendTransactionData(finalQuote.sendTransactionData)

                priceImpactService.setPriceImpact(finalQuote.priceImpact, swapProvider.title)
            } catch (t: Throwable) {
//                Log.e("AAA", "fetchFinalQuote error", t)
            }
        }
    }

    suspend fun swap() = withContext(Dispatchers.Default) {
        stat(page = StatPage.SwapConfirmation, event = StatEvent.Send)

        when (val sendResult = sendTransactionService.sendTransaction()) {
            is SendTransactionResult.Evm -> {
                // call the api for swap transaction here
                sendTransactionDetails(
                    toAddress = sendResult.fullTransaction.transaction.to.toString(),
                    fromAddress = sendResult.fullTransaction.transaction.from.toString(),
                    symbol = uiState.networkFee?.primary?.coinValue?.coin?.code.toString(),
                    txnHash = sendResult.fullTransaction.transaction.hashString,
                    totalAmount = amountIn.toPlainString(),
                    fromContract = getTokenContractAddress(tokenIn),
                    toContract = getTokenContractAddress(tokenOut),
                    type = "swap",
                )
            }

            is SendTransactionResult.Tron -> {
                sendTransactionDetails(
                    toAddress = sendResult.toAddress,
                    fromAddress = sendResult.fromAddress,
                    symbol = sendResult.feeCoinCode,
                    txnHash = sendResult.txId,
                    totalAmount = amountIn.toPlainString(),
                    fromContract = getTokenContractAddress(tokenIn),
                    toContract = getTokenContractAddress(tokenOut),
                    type = "swap",
                )
            }
        }
    }

    private fun getTokenContractAddress(token: Token): String? {
        return when (token.type) {
            is TokenType.AddressTyped -> null
            is TokenType.Bep2 -> null
            is TokenType.Derived -> null
            is TokenType.Native -> null
            is TokenType.Eip20 -> {
                (token.type as TokenType.Eip20).address
            }

            is TokenType.Jetton -> (token.type as TokenType.Jetton).address
            is TokenType.Spl -> {
                (token.type as TokenType.Spl).address
            }

            is TokenType.Unsupported -> {
                (token.type as TokenType.Unsupported).reference
            }
        }
    }

    fun sendTransactionDetails(
        fromAddress: String,
        symbol: String,
        toAddress: String,
        totalAmount: String,
        txnHash: String,
        fromContract: String?,
        toContract: String?,
        type: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            isError.value = null
            isLoading.value = true
            try {
                val result = PayFundRetrofitInstance.payFundApi.sendTransactionDetails(
                    token = getToken(),
                    sendTransactionDetailsRequestModel = SendTransactionDetailsRequestModel(
                        fromAddress = fromAddress,
                        symbol = symbol,
                        toAddress = toAddress,
                        totalAmount = totalAmount.toDouble(),
                        txnHash = txnHash,
                        fromContract = fromContract,
                        toContract = toContract,
                        type = type
                    )
                )
                if (result.isSuccessful && result.body() != null) {
                    val response = result.body()!!

                    sendTransactionDetailsResponseModel.value = response

                } else {
                    isError.value = getError(result)?.message
                }
            } catch (e: Exception) {
                isError.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun getError(result: Response<*>): TwoFAErrorResponseModel? {
        val gson = Gson()
        return result.errorBody()?.charStream()?.let {
            gson.fromJson(it, TwoFAErrorResponseModel::class.java)
        }
    }

    private fun getToken(): String {
        return "Bearer " + tokenManager.crateUserGetToken()
    }

    companion object {
        fun init(
            quote: SwapProviderQuote,
            settings: Map<String, Any?>
        ): CreationExtras.() -> SwapConfirmViewModel = {
            val sendTransactionService =
                SendTransactionServiceFactory.create(quote.tokenIn)

            SwapConfirmViewModel(
                quote.provider,
                quote.swapQuote,
                settings,
                App.currencyManager,
                FiatService(App.marketKit),
                FiatService(App.marketKit),
                FiatService(App.marketKit),
                sendTransactionService,
                TimerService(),
                PriceImpactService()
            )
        }
    }
}


data class SwapConfirmUiState(
    val expiresIn: Long?,
    val expired: Boolean,
    val loading: Boolean,
    val tokenIn: Token,
    val tokenOut: Token,
    val amountIn: BigDecimal,
    val amountOut: BigDecimal?,
    val amountOutMin: BigDecimal?,
    val fiatAmountIn: BigDecimal?,
    val fiatAmountOut: BigDecimal?,
    val fiatAmountOutMin: BigDecimal?,
    val currency: Currency,
    val networkFee: SendModule.AmountData?,
    val cautions: List<CautionViewItem>,
    val validQuote: Boolean,
    val priceImpact: BigDecimal?,
    val priceImpactLevel: PriceImpactLevel?,
    val quoteFields: List<DataField>,
    val transactionFields: List<DataField>,
)