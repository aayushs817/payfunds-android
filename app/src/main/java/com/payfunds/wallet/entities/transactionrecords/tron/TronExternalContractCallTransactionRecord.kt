package com.payfunds.wallet.entities.transactionrecords.tron

import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.tronkit.models.Transaction
import com.payfunds.wallet.core.managers.SpamManager
import com.payfunds.wallet.entities.TransactionValue
import com.payfunds.wallet.entities.transactionrecords.evm.EvmTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.evm.TransferEvent
import com.payfunds.wallet.modules.transactions.TransactionSource

class TronExternalContractCallTransactionRecord(
    transaction: Transaction,
    baseToken: Token,
    source: TransactionSource,
    spamManager: SpamManager,
    val incomingEvents: List<TransferEvent>,
    val outgoingEvents: List<TransferEvent>
) : TronTransactionRecord(
    transaction = transaction,
    baseToken = baseToken,
    source = source,
    foreignTransaction = true,
    spam = spamManager.isSpam(incomingEvents, outgoingEvents)
) {

    override val mainValue: TransactionValue?
        get() {
            val (incomingValues, outgoingValues) = EvmTransactionRecord.combined(
                incomingEvents,
                outgoingEvents
            )

            return when {
                (incomingValues.isEmpty() && outgoingValues.size == 1) -> outgoingValues.first()
                (incomingValues.size == 1 && outgoingValues.isEmpty()) -> incomingValues.first()
                else -> null
            }
        }

}
