package com.payfunds.wallet.modules.send.solana

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import cash.z.ecc.android.sdk.ext.collectWith
import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.marketkit.models.TokenType
import io.horizontalsystems.solanakit.SolanaKit
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.EvmError
import com.payfunds.wallet.core.HSCaution
import com.payfunds.wallet.core.ISendSolanaAdapter
import com.payfunds.wallet.core.LocalizedException
import com.payfunds.wallet.core.ViewModelUiState
import com.payfunds.wallet.core.managers.ConnectivityManager
import com.payfunds.wallet.entities.Address
import com.payfunds.wallet.entities.Wallet
import com.payfunds.wallet.modules.amount.SendAmountService
import com.payfunds.wallet.modules.contacts.ContactsRepository
import com.payfunds.wallet.modules.send.SendConfirmationData
import com.payfunds.wallet.modules.send.SendErrorInsufficientBalance
import com.payfunds.wallet.modules.send.SendResult
import com.payfunds.wallet.modules.send.SendUiState
import com.payfunds.wallet.modules.xrate.XRateService
import com.payfunds.wallet.ui.compose.TranslatableString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.net.UnknownHostException

class SendSolanaViewModel(
    val wallet: Wallet,
    val sendToken: Token,
    val feeToken: Token,
    val solBalance: BigDecimal,
    val adapter: ISendSolanaAdapter,
    private val xRateService: XRateService,
    private val amountService: SendAmountService,
    private val addressService: SendSolanaAddressService,
    val coinMaxAllowedDecimals: Int,
    private val contactsRepo: ContactsRepository,
    private val showAddressInput: Boolean,
    private val connectivityManager: ConnectivityManager,
) : ViewModelUiState<SendUiState>() {
    val blockchainType = wallet.token.blockchainType
    val feeTokenMaxAllowedDecimals = feeToken.decimals
    val fiatMaxAllowedDecimals = App.appConfigProvider.fiatDecimal

    private var amountState = amountService.stateFlow.value
    private var addressState = addressService.stateFlow.value

    var coinRate by mutableStateOf(xRateService.getRate(sendToken.coin.uid))
        private set
    var feeCoinRate by mutableStateOf(xRateService.getRate(feeToken.coin.uid))
        private set
    var sendResult by mutableStateOf<SendResult?>(null)
        private set
    private val decimalAmount: BigDecimal
        get() = amountState.amount!!

    init {
        amountService.stateFlow.collectWith(viewModelScope) {
            handleUpdatedAmountState(it)
        }
        addressService.stateFlow.collectWith(viewModelScope) {
            handleUpdatedAddressState(it)
        }
        xRateService.getRateFlow(sendToken.coin.uid).collectWith(viewModelScope) {
            coinRate = it
        }
        xRateService.getRateFlow(feeToken.coin.uid).collectWith(viewModelScope) {
            feeCoinRate = it
        }
    }

    override fun createState() = SendUiState(
        availableBalance = amountState.availableBalance,
        amountCaution = amountState.amountCaution,
        addressError = addressState.addressError,
        canBeSend = amountState.canBeSend && addressState.canBeSend,
        showAddressInput = showAddressInput,
    )

    fun onEnterAmount(amount: BigDecimal?) {
        amountService.setAmount(amount)
    }

    fun onEnterAddress(address: Address?) {
        addressService.setAddress(address)
    }

    fun getConfirmationData(): SendConfirmationData {
        val address = addressState.address!!
        val contact = contactsRepo.getContactsFiltered(
            blockchainType,
            addressQuery = address.hex
        ).firstOrNull()
        return SendConfirmationData(
            amount = decimalAmount,
            fee = SolanaKit.fee,
            address = address,
            contact = contact,
            coin = wallet.coin,
            feeCoin = feeToken.coin,
            memo = null
        )
    }

    fun onClickSend() {
        viewModelScope.launch {
            send()
        }
    }

    fun hasConnection(): Boolean {
        return connectivityManager.isConnected
    }

    private suspend fun send() = withContext(Dispatchers.IO) {
        if (!hasConnection()) {
            sendResult = SendResult.Failed(createCaution(UnknownHostException()))
            return@withContext
        }

        try {
            sendResult = SendResult.Sending

            val totalSolAmount =
                (if (sendToken.type == TokenType.Native) decimalAmount else BigDecimal.ZERO) + SolanaKit.fee

            if (totalSolAmount > solBalance)
                throw EvmError.InsufficientBalanceWithFee

            adapter.send(decimalAmount, addressState.evmAddress!!)

            sendResult = SendResult.Sent
        } catch (e: Throwable) {
            sendResult = SendResult.Failed(createCaution(e))
        }
    }

    private fun createCaution(error: Throwable) = when (error) {
        is UnknownHostException -> HSCaution(TranslatableString.ResString(R.string.Hud_Text_NoInternet))
        is LocalizedException -> HSCaution(TranslatableString.ResString(error.errorTextRes))
        is EvmError.InsufficientBalanceWithFee -> SendErrorInsufficientBalance(feeToken.coin.code)
        else -> HSCaution(
            TranslatableString.PlainString(
                error.cause?.message ?: error.message ?: ""
            )
        )
    }

    private fun handleUpdatedAmountState(amountState: SendAmountService.State) {
        this.amountState = amountState

        emitState()
    }

    private fun handleUpdatedAddressState(addressState: SendSolanaAddressService.State) {
        this.addressState = addressState

        emitState()
    }

}
