package com.payfunds.wallet.modules.eip20approve

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.horizontalsystems.ethereumkit.models.Address
import io.horizontalsystems.marketkit.models.Token
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.IAdapterManager
import com.payfunds.wallet.core.IWalletManager
import com.payfunds.wallet.core.ViewModelUiState
import com.payfunds.wallet.core.adapters.Eip20Adapter
import com.payfunds.wallet.core.ethereum.CautionViewItem
import com.payfunds.wallet.core.managers.CurrencyManager
import com.payfunds.wallet.entities.Currency
import com.payfunds.wallet.modules.contacts.ContactsRepository
import com.payfunds.wallet.modules.contacts.model.Contact
import com.payfunds.wallet.modules.eip20approve.AllowanceMode.OnlyRequired
import com.payfunds.wallet.modules.eip20approve.AllowanceMode.Unlimited
import com.payfunds.wallet.modules.multiswap.FiatService
import com.payfunds.wallet.modules.multiswap.sendtransaction.SendTransactionData
import com.payfunds.wallet.modules.multiswap.sendtransaction.SendTransactionServiceEvm
import com.payfunds.wallet.modules.send.SendModule
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class Eip20ApproveViewModel(
    private val token: Token,
    private val requiredAllowance: BigDecimal,
    private val spenderAddress: String,
    private val walletManager: IWalletManager,
    private val adapterManager: IAdapterManager,
    val sendTransactionService: SendTransactionServiceEvm,
    private val currencyManager: CurrencyManager,
    private val fiatService: FiatService,
    private val contactsRepository: ContactsRepository,
) : ViewModelUiState<Eip20ApproveUiState>() {
    private val currency = currencyManager.baseCurrency
    private var allowanceMode = OnlyRequired
    private var sendTransactionState = sendTransactionService.stateFlow.value
    private var fiatAmount: BigDecimal? = null
    private val contact = contactsRepository.getContactsFiltered(
        blockchainType = token.blockchainType,
        addressQuery = spenderAddress
    ).firstOrNull()

    override fun createState() = Eip20ApproveUiState(
        token = token,
        requiredAllowance = requiredAllowance,
        allowanceMode = allowanceMode,
        networkFee = sendTransactionState.networkFee,
        cautions = sendTransactionState.cautions,
        currency = currency,
        fiatAmount = fiatAmount,
        spenderAddress = spenderAddress,
        contact = contact,
        approveEnabled = sendTransactionState.sendable
    )

    init {

        fiatService.setCurrency(currency)
        fiatService.setToken(token)
        fiatService.setAmount(requiredAllowance)

        viewModelScope.launch {
            fiatService.stateFlow.collect {
                fiatAmount = it.fiatAmount
                emitState()
            }
        }

        viewModelScope.launch {
            sendTransactionService.stateFlow.collect { transactionState ->
                sendTransactionState = transactionState
                emitState()
            }
        }

        sendTransactionService.start(viewModelScope)
    }

    fun setAllowanceMode(allowanceMode: AllowanceMode) {
        this.allowanceMode = allowanceMode

        emitState()
    }

    fun freeze() {
        val eip20Adapter =
            walletManager.activeWallets.firstOrNull { it.token == token }?.let { wallet ->
                adapterManager.getAdapterForWallet(wallet) as? Eip20Adapter
            }

        checkNotNull(eip20Adapter)

        val transactionData = when (allowanceMode) {
            OnlyRequired -> eip20Adapter.buildApproveTransactionData(
                Address(spenderAddress),
                requiredAllowance
            )

            Unlimited -> eip20Adapter.buildApproveUnlimitedTransactionData(Address(spenderAddress))
        }

        sendTransactionService.setSendTransactionData(
            SendTransactionData.Evm(
                transactionData,
                null
            )
        )
    }

    suspend fun approve() = withContext(Dispatchers.Default) {
        sendTransactionService.sendTransaction()
    }

    class Factory(
        private val token: Token,
        private val requiredAllowance: BigDecimal,
        private val spenderAddress: String,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val sendTransactionService = SendTransactionServiceEvm(token.blockchainType)

            return Eip20ApproveViewModel(
                token,
                requiredAllowance,
                spenderAddress,
                App.walletManager,
                App.adapterManager,
                sendTransactionService,
                App.currencyManager,
                FiatService(App.marketKit),
                App.contactsRepository
            ) as T
        }
    }
}

data class Eip20ApproveUiState(
    val token: Token,
    val requiredAllowance: BigDecimal,
    val allowanceMode: AllowanceMode,
    val networkFee: SendModule.AmountData?,
    val cautions: List<CautionViewItem>,
    val currency: Currency,
    val fiatAmount: BigDecimal?,
    val spenderAddress: String,
    val contact: Contact?,
    val approveEnabled: Boolean,
)

enum class AllowanceMode {
    OnlyRequired, Unlimited
}

