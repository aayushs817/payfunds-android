package com.payfunds.wallet.entities.transactionrecords.solana

import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.solanakit.models.Transaction
import com.payfunds.wallet.modules.transactions.TransactionSource

class SolanaUnknownTransactionRecord(
    transaction: Transaction,
    baseToken: Token,
    source: TransactionSource,
    val incomingTransfers: List<Transfer>,
    val outgoingTransfers: List<Transfer>
) : SolanaTransactionRecord(transaction, baseToken, source)
