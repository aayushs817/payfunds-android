package com.payfunds.wallet.modules.send.evm.confirmation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.horizontalsystems.ethereumkit.models.TransactionData
import io.horizontalsystems.marketkit.models.BlockchainType
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.ViewModelUiState
import com.payfunds.wallet.core.ethereum.CautionViewItem
import com.payfunds.wallet.core.ethereum.EvmCoinServiceFactory
import com.payfunds.wallet.modules.multiswap.sendtransaction.SendTransactionData
import com.payfunds.wallet.modules.multiswap.sendtransaction.SendTransactionServiceEvm
import com.payfunds.wallet.modules.multiswap.ui.DataField
import com.payfunds.wallet.modules.send.SendModule
import com.payfunds.wallet.modules.send.evm.SendEvmData
import com.payfunds.wallet.modules.sendevmtransaction.SectionViewItem
import com.payfunds.wallet.modules.sendevmtransaction.SendEvmTransactionViewItemFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SendEvmConfirmationViewModel(
    private val sendEvmTransactionViewItemFactory: SendEvmTransactionViewItemFactory,
    val sendTransactionService: SendTransactionServiceEvm,
    val transactionData: TransactionData,
    private val additionalInfo: SendEvmData.AdditionalInfo?,
    val contractAddress: String?,
    val amount: String?
) : ViewModelUiState<SendEvmConfirmationUiState>() {
    private var sendTransactionState = sendTransactionService.stateFlow.value

    private val sectionViewItems = sendEvmTransactionViewItemFactory.getItems(
        transactionData,
        additionalInfo,
        sendTransactionService.decorate(transactionData)
    )

    init {
        viewModelScope.launch {
            sendTransactionService.stateFlow.collect { transactionState ->
                sendTransactionState = transactionState
                emitState()
            }
        }

        sendTransactionService.start(viewModelScope)

        sendTransactionService.setSendTransactionData(
            SendTransactionData.Evm(
                transactionData,
                null
            )
        )
    }

    override fun createState() = SendEvmConfirmationUiState(
        networkFee = sendTransactionState.networkFee,
        cautions = sendTransactionState.cautions,
        sendEnabled = sendTransactionState.sendable,
        transactionFields = sendTransactionState.fields,
        sectionViewItems = sectionViewItems
    )

    suspend fun send() = withContext(Dispatchers.Default) {
        sendTransactionService.sendTransaction()
    }

    class Factory(
        private val transactionData: TransactionData,
        private val additionalInfo: SendEvmData.AdditionalInfo?,
        private val blockchainType: BlockchainType,
        private val contractAddress: String?,
        private val amount: String? = null,
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val sendTransactionService = SendTransactionServiceEvm(blockchainType)
            val feeToken = App.evmBlockchainManager.getBaseToken(blockchainType)!!
            val coinServiceFactory = EvmCoinServiceFactory(
                feeToken,
                App.marketKit,
                App.currencyManager,
                App.coinManager
            )

            val sendEvmTransactionViewItemFactory = SendEvmTransactionViewItemFactory(
                App.evmLabelManager,
                coinServiceFactory,
                App.contactsRepository,
                blockchainType
            )

            return SendEvmConfirmationViewModel(
                sendEvmTransactionViewItemFactory,
                sendTransactionService,
                transactionData,
                additionalInfo,
                contractAddress,
                amount
            ) as T
        }
    }

}

data class SendEvmConfirmationUiState(
    val networkFee: SendModule.AmountData?,
    val cautions: List<CautionViewItem>,
    val sendEnabled: Boolean,
    val transactionFields: List<DataField>,
    val sectionViewItems: List<SectionViewItem>
)