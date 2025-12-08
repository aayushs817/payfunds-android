package com.payfunds.wallet.entities.transactionrecords.solana

import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.solanakit.models.Transaction
import com.payfunds.wallet.entities.TransactionValue
import com.payfunds.wallet.modules.transactions.TransactionSource

class SolanaIncomingTransactionRecord(
    transaction: Transaction,
    baseToken: Token,
    source: TransactionSource,
    val from: String?,
    val value: TransactionValue
) : SolanaTransactionRecord(transaction, baseToken, source) {

    override val mainValue = value

}