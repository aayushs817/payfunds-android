package com.payfunds.wallet.entities.transactionrecords.tron

import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.tronkit.models.Transaction
import com.payfunds.wallet.entities.TransactionValue
import com.payfunds.wallet.modules.transactions.TransactionSource

class TronIncomingTransactionRecord(
    transaction: Transaction,
    baseToken: Token,
    source: TransactionSource,
    val from: String,
    val value: TransactionValue,
    spam: Boolean
) : TronTransactionRecord(transaction, baseToken, source, true, spam) {

    override val mainValue = value

}
