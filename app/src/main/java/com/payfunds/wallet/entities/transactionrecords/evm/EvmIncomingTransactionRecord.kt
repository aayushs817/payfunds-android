package com.payfunds.wallet.entities.transactionrecords.evm

import io.horizontalsystems.ethereumkit.models.Transaction
import io.horizontalsystems.marketkit.models.Token
import com.payfunds.wallet.core.managers.SpamManager
import com.payfunds.wallet.entities.TransactionValue
import com.payfunds.wallet.modules.transactions.TransactionSource

class EvmIncomingTransactionRecord(
    transaction: Transaction,
    baseToken: Token,
    source: TransactionSource,
    spamManager: SpamManager,
    val from: String,
    val value: TransactionValue
) : EvmTransactionRecord(
    transaction = transaction,
    baseToken = baseToken,
    source = source,
    foreignTransaction = true,
    spam = spamManager.isIncomingSpam(value)
) {

    override val mainValue = value

}
