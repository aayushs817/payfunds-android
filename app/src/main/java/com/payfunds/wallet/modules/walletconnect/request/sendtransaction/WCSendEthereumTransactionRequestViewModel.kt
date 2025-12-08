package com.payfunds.wallet.modules.walletconnect.request.sendtransaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import io.horizontalsystems.ethereumkit.models.TransactionData
import io.horizontalsystems.marketkit.models.BlockchainType
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.ViewModelUiState
import com.payfunds.wallet.core.ethereum.CautionViewItem
import com.payfunds.wallet.core.ethereum.EvmCoinServiceFactory
import com.payfunds.wallet.core.providers.Translator
import com.payfunds.wallet.core.toHexString
import com.payfunds.wallet.modules.multiswap.sendtransaction.SendTransactionData
import com.payfunds.wallet.modules.multiswap.sendtransaction.SendTransactionServiceEvm
import com.payfunds.wallet.modules.multiswap.sendtransaction.SendTransactionServiceState
import com.payfunds.wallet.modules.multiswap.ui.DataField
import com.payfunds.wallet.modules.nft.collection.events.NftCollectionEventsViewModel
import com.payfunds.wallet.modules.send.SendModule
import com.payfunds.wallet.modules.sendevmtransaction.SectionViewItem
import com.payfunds.wallet.modules.sendevmtransaction.SendEvmTransactionViewItemFactory
import com.payfunds.wallet.modules.sendevmtransaction.ValueType
import com.payfunds.wallet.modules.sendevmtransaction.ViewItem
import com.payfunds.wallet.modules.walletconnect.WCDelegate
import com.payfunds.wallet.modules.walletconnect.request.WCChainData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WCSendEthereumTransactionRequestViewModel(
    private val sendEvmTransactionViewItemFactory: SendEvmTransactionViewItemFactory,
    private val dAppName: String,
    transaction: WalletConnectTransaction,
    blockchainType: BlockchainType
) : ViewModelUiState<WCSendEthereumTransactionRequestUiState>() {
    val sendTransactionService: SendTransactionServiceEvm

    private val transactionData = TransactionData(
        transaction.to,
        transaction.value,
        transaction.data
    )

    private var sendTransactionState: SendTransactionServiceState

    init {
        sendTransactionService = SendTransactionServiceEvm(
            blockchainType = blockchainType,
            initialGasPrice = transaction.getGasPriceObj(),
            initialNonce = transaction.nonce
        )
        sendTransactionState = sendTransactionService.stateFlow.value

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

    override fun createState() = WCSendEthereumTransactionRequestUiState(
        networkFee = sendTransactionState.networkFee,
        cautions = sendTransactionState.cautions,
        sendEnabled = sendTransactionState.sendable,
        transactionFields = sendTransactionState.fields,
        sectionViewItems = getSectionViewItems()
    )

    private fun getSectionViewItems(): List<SectionViewItem> {
        val items = sendEvmTransactionViewItemFactory.getItems(
            transactionData,
            null,
            sendTransactionService.decorate(transactionData)
        ) + SectionViewItem(
            buildList {
                add(
                    ViewItem.Value(
                        Translator.getString(R.string.WalletConnect_SignMessageRequest_dApp),
                        dAppName,
                        ValueType.Regular
                    )
                )

                val chain: WCChainData? = null // todo: need to implement it
                chain?.let {
                    add(
                        ViewItem.Value(
                            it.chain.name,
                            it.address ?: "",
                            ValueType.Regular
                        )
                    )
                }
            }
        )

        return items
    }

    suspend fun confirm() = withContext(Dispatchers.Default) {
        val sendResult = sendTransactionService.sendTransaction()
        val transactionHash = sendResult.fullTransaction.transaction.hash

        WCDelegate.sessionRequestEvent?.let { sessionRequest ->
            WCDelegate.respondPendingRequest(
                sessionRequest.request.id,
                sessionRequest.topic,
                transactionHash.toHexString()
            )
        }
    }

    fun reject() {
        WCDelegate.sessionRequestEvent?.let { sessionRequest ->
            WCDelegate.rejectRequest(sessionRequest.topic, sessionRequest.request.id)
        }
    }

    class Factory(
        private val blockchainType: BlockchainType,
        private val transaction: WalletConnectTransaction,
        private val peerName: String
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
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

            return WCSendEthereumTransactionRequestViewModel(
                sendEvmTransactionViewItemFactory,
                peerName,
                transaction,
                blockchainType
            ) as T
        }
    }
}

data class WCSendEthereumTransactionRequestUiState(
    val networkFee: SendModule.AmountData?,
    val cautions: List<CautionViewItem>,
    val sendEnabled: Boolean,
    val transactionFields: List<DataField>,
    val sectionViewItems: List<SectionViewItem>
)
