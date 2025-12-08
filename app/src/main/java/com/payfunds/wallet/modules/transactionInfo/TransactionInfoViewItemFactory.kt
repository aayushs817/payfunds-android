package com.payfunds.wallet.modules.transactionInfo

import com.payfunds.wallet.R
import com.payfunds.wallet.core.adapters.TonTransactionRecord
import com.payfunds.wallet.core.managers.TonHelper
import com.payfunds.wallet.core.providers.Translator
import com.payfunds.wallet.entities.transactionrecords.binancechain.BinanceChainIncomingTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.binancechain.BinanceChainOutgoingTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.bitcoin.BitcoinIncomingTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.bitcoin.BitcoinOutgoingTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.evm.ApproveTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.evm.ContractCallTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.evm.ContractCreationTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.evm.EvmIncomingTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.evm.EvmOutgoingTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.evm.EvmTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.evm.ExternalContractCallTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.evm.SwapTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.evm.UnknownSwapTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.solana.SolanaIncomingTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.solana.SolanaOutgoingTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.solana.SolanaUnknownTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.tron.TronApproveTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.tron.TronContractCallTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.tron.TronExternalContractCallTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.tron.TronIncomingTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.tron.TronOutgoingTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.tron.TronTransactionRecord
import com.payfunds.wallet.modules.transactionInfo.TransactionInfoViewItem.SentToSelf
import com.payfunds.wallet.modules.transactionInfo.TransactionInfoViewItem.SpeedUpCancel
import com.payfunds.wallet.modules.transactionInfo.TransactionInfoViewItem.Transaction
import com.payfunds.wallet.modules.transactionInfo.TransactionViewItemFactoryHelper.getSwapEventSectionItems
import com.payfunds.wallet.modules.transactions.TransactionStatus
import com.payfunds.wallet.modules.transactions.TransactionViewItem
import io.horizontalsystems.marketkit.models.BlockchainType

class TransactionInfoViewItemFactory(
    private val resendEnabled: Boolean,
    private val blockchainType: BlockchainType,
) {
    fun getViewItemSections(transactionItem: TransactionInfoItem): List<List<TransactionInfoViewItem>> {
        val transaction = transactionItem.record
        val rates = transactionItem.rates
        val nftMetadata = transactionItem.nftMetadata

        val status = transaction.status(transactionItem.lastBlockInfo?.height)
        val itemSections = mutableListOf<List<TransactionInfoViewItem>>()
        val miscItemsSection = mutableListOf<TransactionInfoViewItem>()

        var sentToSelf = false

        if (transactionItem.record.spam) {
            itemSections.add(listOf(TransactionInfoViewItem.WarningMessage(Translator.getString(R.string.TransactionInfo_SpamWarning))))
        }

        when (transaction) {
            is ContractCreationTransactionRecord -> {
                itemSections.add(
                    TransactionViewItemFactoryHelper.getContractCreationItems(
                        transaction
                    )
                )
            }

            is TonTransactionRecord -> {
                transaction.actions.forEach { action ->
                    itemSections.add(
                        TonHelper.getViewItemsForAction(
                            action,
                            rates,
                            blockchainType,
                            transactionItem.hideAmount
                        )
                    )
                }

//            feeViewItem = record.fee.map { .fee(title: "tx_info.fee".localized, value: feeString(transactionValue: $0, rate: _rate($0))) }
            }

            is EvmIncomingTransactionRecord ->
                itemSections.add(
                    TransactionViewItemFactoryHelper.getReceiveSectionItems(
                        value = transaction.value,
                        fromAddress = transaction.from,
                        coinPrice = rates[transaction.value.coinUid],
                        hideAmount = transactionItem.hideAmount,
                        blockchainType = blockchainType,
                    )
                )

            is TronIncomingTransactionRecord ->
                itemSections.add(
                    TransactionViewItemFactoryHelper.getReceiveSectionItems(
                        value = transaction.value,
                        fromAddress = transaction.from,
                        coinPrice = rates[transaction.value.coinUid],
                        hideAmount = transactionItem.hideAmount,
                        blockchainType = blockchainType,
                    )
                )

            is EvmOutgoingTransactionRecord -> {
                sentToSelf = transaction.sentToSelf
                itemSections.add(
                    TransactionViewItemFactoryHelper.getSendSectionItems(
                        value = transaction.value,
                        toAddress = transaction.to,
                        coinPrice = rates[transaction.value.coinUid],
                        hideAmount = transactionItem.hideAmount,
                        sentToSelf = transaction.sentToSelf,
                        nftMetadata = nftMetadata,
                        blockchainType = blockchainType,
                    )
                )
            }

            is TronOutgoingTransactionRecord -> {
                sentToSelf = transaction.sentToSelf
                itemSections.add(
                    TransactionViewItemFactoryHelper.getSendSectionItems(
                        value = transaction.value,
                        toAddress = transaction.to,
                        coinPrice = rates[transaction.value.coinUid],
                        hideAmount = transactionItem.hideAmount,
                        sentToSelf = transaction.sentToSelf,
                        nftMetadata = nftMetadata,
                        blockchainType = blockchainType,
                    )
                )
            }

            is SwapTransactionRecord -> {
                itemSections.add(
                    getSwapEventSectionItems(
                        valueIn = transaction.valueIn,
                        valueOut = transaction.valueOut,
                        rates = rates,
                        amount = transaction.amountIn,
                        hideAmount = transactionItem.hideAmount,
                        hasRecipient = transaction.recipient != null
                    )
                )

                itemSections.add(
                    TransactionViewItemFactoryHelper.getSwapDetailsSectionItems(
                        rates,
                        transaction.exchangeAddress,
                        transaction.valueOut,
                        transaction.valueIn
                    )
                )
            }

            is UnknownSwapTransactionRecord -> {
                itemSections.add(
                    getSwapEventSectionItems(
                        valueIn = transaction.valueIn,
                        valueOut = transaction.valueOut,
                        amount = null,
                        rates = rates,
                        hideAmount = transactionItem.hideAmount,
                        hasRecipient = false
                    )
                )

                itemSections.add(
                    TransactionViewItemFactoryHelper.getSwapDetailsSectionItems(
                        rates,
                        transaction.exchangeAddress,
                        transaction.valueOut,
                        transaction.valueIn,
                    )
                )
            }

            is ApproveTransactionRecord ->
                itemSections.add(
                    TransactionViewItemFactoryHelper.getApproveSectionItems(
                        value = transaction.value,
                        coinPrice = rates[transaction.value.coinUid],
                        spenderAddress = transaction.spender,
                        hideAmount = transactionItem.hideAmount,
                        blockchainType = blockchainType,
                    )
                )

            is TronApproveTransactionRecord ->
                itemSections.add(
                    TransactionViewItemFactoryHelper.getApproveSectionItems(
                        value = transaction.value,
                        coinPrice = rates[transaction.value.coinUid],
                        spenderAddress = transaction.spender,
                        hideAmount = transactionItem.hideAmount,
                        blockchainType = blockchainType,
                    )
                )

            is ContractCallTransactionRecord -> {
                itemSections.add(
                    TransactionViewItemFactoryHelper.getContractMethodSectionItems(
                        transaction.method,
                        transaction.contractAddress,
                        transaction.blockchainType
                    )
                )

                for (event in transaction.outgoingEvents) {
                    itemSections.add(
                        TransactionViewItemFactoryHelper.getSendSectionItems(
                            value = event.value,
                            toAddress = event.address,
                            coinPrice = rates[event.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata,
                            blockchainType = blockchainType,
                        )
                    )
                }

                for (event in transaction.incomingEvents) {
                    itemSections.add(
                        TransactionViewItemFactoryHelper.getReceiveSectionItems(
                            value = event.value,
                            fromAddress = event.address,
                            coinPrice = rates[event.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata,
                            blockchainType = blockchainType,
                        )
                    )
                }
            }

            is TronContractCallTransactionRecord -> {
                itemSections.add(
                    TransactionViewItemFactoryHelper.getContractMethodSectionItems(
                        transaction.method,
                        transaction.contractAddress,
                        transaction.blockchainType
                    )
                )

                for (event in transaction.outgoingEvents) {
                    itemSections.add(
                        TransactionViewItemFactoryHelper.getSendSectionItems(
                            value = event.value,
                            toAddress = event.address,
                            coinPrice = rates[event.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata,
                            blockchainType = blockchainType,
                        )
                    )
                }

                for (event in transaction.incomingEvents) {
                    itemSections.add(
                        TransactionViewItemFactoryHelper.getReceiveSectionItems(
                            value = event.value,
                            fromAddress = event.address,
                            coinPrice = rates[event.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata,
                            blockchainType = blockchainType,
                        )
                    )
                }
            }

            is ExternalContractCallTransactionRecord -> {
                for (event in transaction.outgoingEvents) {
                    itemSections.add(
                        TransactionViewItemFactoryHelper.getSendSectionItems(
                            value = event.value,
                            toAddress = event.address,
                            coinPrice = rates[event.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata,
                            blockchainType = blockchainType,
                        )
                    )
                }

                for (event in transaction.incomingEvents) {
                    itemSections.add(
                        TransactionViewItemFactoryHelper.getReceiveSectionItems(
                            value = event.value,
                            fromAddress = event.address,
                            coinPrice = rates[event.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata,
                            blockchainType = blockchainType,
                        )
                    )
                }
            }

            is TronExternalContractCallTransactionRecord -> {
                for (event in transaction.outgoingEvents) {
                    itemSections.add(
                        TransactionViewItemFactoryHelper.getSendSectionItems(
                            value = event.value,
                            toAddress = event.address,
                            coinPrice = rates[event.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata,
                            blockchainType = blockchainType,
                        )
                    )
                }

                for (event in transaction.incomingEvents) {
                    itemSections.add(
                        TransactionViewItemFactoryHelper.getReceiveSectionItems(
                            value = event.value,
                            fromAddress = event.address,
                            coinPrice = rates[event.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata,
                            blockchainType = blockchainType,
                        )
                    )
                }
            }

            is TronTransactionRecord -> {
                itemSections.add(
                    listOf(
                        Transaction(
                            transaction.transaction.contract?.label
                                ?: Translator.getString(R.string.Transactions_ContractCall),
                            "",
                            TransactionViewItem.Icon.Platform(transaction.blockchainType).iconRes
                        )
                    )
                )
            }

            is BitcoinIncomingTransactionRecord -> {
                itemSections.add(
                    TransactionViewItemFactoryHelper.getReceiveSectionItems(
                        value = transaction.value,
                        fromAddress = transaction.from,
                        coinPrice = rates[transaction.value.coinUid],
                        hideAmount = transactionItem.hideAmount,
                        blockchainType = blockchainType,
                    )
                )

                miscItemsSection.addAll(
                    TransactionViewItemFactoryHelper.getBitcoinSectionItems(
                        transaction,
                        transactionItem.lastBlockInfo
                    )
                )
                addMemoItem(transaction.memo, miscItemsSection)
            }

            is BitcoinOutgoingTransactionRecord -> {
                sentToSelf = transaction.sentToSelf
                itemSections.add(
                    TransactionViewItemFactoryHelper.getSendSectionItems(
                        value = transaction.value,
                        toAddress = transaction.to,
                        coinPrice = rates[transaction.value.coinUid],
                        hideAmount = transactionItem.hideAmount,
                        sentToSelf = transaction.sentToSelf,
                        blockchainType = blockchainType,
                    )
                )

                miscItemsSection.addAll(
                    TransactionViewItemFactoryHelper.getBitcoinSectionItems(
                        transaction,
                        transactionItem.lastBlockInfo
                    )
                )
                addMemoItem(transaction.memo, miscItemsSection)
            }

            is BinanceChainIncomingTransactionRecord -> {
                itemSections.add(
                    TransactionViewItemFactoryHelper.getReceiveSectionItems(
                        value = transaction.value,
                        fromAddress = transaction.from,
                        coinPrice = rates[transaction.value.coinUid],
                        hideAmount = transactionItem.hideAmount,
                        blockchainType = blockchainType,
                    )
                )

                addMemoItem(transaction.memo, miscItemsSection)
            }

            is BinanceChainOutgoingTransactionRecord -> {
                sentToSelf = transaction.sentToSelf
                itemSections.add(
                    TransactionViewItemFactoryHelper.getSendSectionItems(
                        value = transaction.value,
                        toAddress = transaction.to,
                        coinPrice = rates[transaction.value.coinUid],
                        hideAmount = transactionItem.hideAmount,
                        sentToSelf = transaction.sentToSelf,
                        blockchainType = blockchainType,
                    )
                )

                addMemoItem(transaction.memo, miscItemsSection)
            }

            is SolanaIncomingTransactionRecord ->
                itemSections.add(
                    TransactionViewItemFactoryHelper.getReceiveSectionItems(
                        value = transaction.value,
                        fromAddress = transaction.from,
                        coinPrice = rates[transaction.value.coinUid],
                        hideAmount = transactionItem.hideAmount,
                        nftMetadata = nftMetadata,
                        blockchainType = blockchainType,
                    )
                )

            is SolanaOutgoingTransactionRecord -> {
                sentToSelf = transaction.sentToSelf
                itemSections.add(
                    TransactionViewItemFactoryHelper.getSendSectionItems(
                        value = transaction.value,
                        toAddress = transaction.to,
                        coinPrice = rates[transaction.value.coinUid],
                        hideAmount = transactionItem.hideAmount,
                        sentToSelf = transaction.sentToSelf,
                        nftMetadata = nftMetadata,
                        blockchainType = blockchainType,
                    )
                )
            }

            is SolanaUnknownTransactionRecord -> {
                for (transfer in transaction.outgoingTransfers) {
                    itemSections.add(
                        TransactionViewItemFactoryHelper.getSendSectionItems(
                            value = transfer.value,
                            toAddress = transfer.address,
                            coinPrice = rates[transfer.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata,
                            blockchainType = blockchainType,
                        )
                    )
                }

                for (transfer in transaction.incomingTransfers) {
                    itemSections.add(
                        TransactionViewItemFactoryHelper.getReceiveSectionItems(
                            value = transfer.value,
                            fromAddress = transfer.address,
                            coinPrice = rates[transfer.value.coinUid],
                            hideAmount = transactionItem.hideAmount,
                            nftMetadata = nftMetadata,
                            blockchainType = blockchainType,
                        )
                    )
                }
            }

            else -> {}
        }

        if (sentToSelf) {
            miscItemsSection.add(SentToSelf)
        }
        if (miscItemsSection.isNotEmpty()) {
            itemSections.add(miscItemsSection)
        }

        itemSections.add(
            TransactionViewItemFactoryHelper.getStatusSectionItems(
                transaction,
                status,
                rates,
                blockchainType
            )
        )
        if (transaction is EvmTransactionRecord && !transaction.foreignTransaction && status == TransactionStatus.Pending && resendEnabled) {
            itemSections.add(
                listOf(
                    SpeedUpCancel(
                        transactionHash = transaction.transactionHash,
                        blockchainType = transaction.blockchainType
                    )
                )
            )
            itemSections.add(listOf(TransactionInfoViewItem.Description(Translator.getString(R.string.TransactionInfo_SpeedUpDescription))))
        } else if (transaction is BitcoinOutgoingTransactionRecord && transaction.replaceable && resendEnabled) {
            itemSections.add(
                listOf(
                    SpeedUpCancel(
                        transactionHash = transaction.transactionHash,
                        blockchainType = transaction.blockchainType
                    )
                )
            )
            itemSections.add(listOf(TransactionInfoViewItem.Description(Translator.getString(R.string.TransactionInfo_SpeedUpDescription))))
        }
        itemSections.add(TransactionViewItemFactoryHelper.getExplorerSectionItems(transactionItem.explorerData))

        return itemSections
    }

    private fun addMemoItem(
        memo: String?,
        miscItemsSection: MutableList<TransactionInfoViewItem>,
    ) {
        if (!memo.isNullOrBlank()) {
            miscItemsSection.add(
                TransactionViewItemFactoryHelper.getMemoItem(memo)
            )
        }
    }
}
