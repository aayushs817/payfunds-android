package com.payfunds.wallet.entities.transactionrecords.tron

import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.tronkit.models.Transaction
import com.payfunds.wallet.entities.TransactionValue
import com.payfunds.wallet.modules.transactions.TransactionSource

class TronApproveTransactionRecord(
    transaction: Transaction,
    baseToken: Token,
    source: TransactionSource,
    val spender: String,
    val value: TransactionValue
) : TronTransactionRecord(transaction, baseToken, source) {

    override val mainValue = value

}
