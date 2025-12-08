package com.payfunds.wallet.entities.transactionrecords.solana

import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.solanakit.models.Transaction
import com.payfunds.wallet.entities.TransactionValue
import com.payfunds.wallet.modules.transactions.TransactionSource

class SolanaOutgoingTransactionRecord(
    transaction: Transaction,
    baseToken: Token,
    source: TransactionSource,
    val to: String?,
    val value: TransactionValue,
    val sentToSelf: Boolean
) : SolanaTransactionRecord(transaction, baseToken, source) {

    override val mainValue = value

}
