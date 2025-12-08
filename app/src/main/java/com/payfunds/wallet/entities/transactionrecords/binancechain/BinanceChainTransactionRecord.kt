package com.payfunds.wallet.entities.transactionrecords.binancechain

import io.horizontalsystems.binancechainkit.models.TransactionInfo
import io.horizontalsystems.marketkit.models.Token
import com.payfunds.wallet.core.adapters.BinanceAdapter
import com.payfunds.wallet.entities.TransactionValue
import com.payfunds.wallet.entities.transactionrecords.TransactionRecord
import com.payfunds.wallet.modules.transactions.TransactionSource

abstract class BinanceChainTransactionRecord(
    transaction: TransactionInfo,
    feeToken: Token,
    source: TransactionSource
) : TransactionRecord(
    uid = transaction.hash,
    transactionHash = transaction.hash,
    transactionIndex = 0,
    blockHeight = transaction.blockNumber,
    confirmationsThreshold = BinanceAdapter.confirmationsThreshold,
    timestamp = transaction.date.time / 1000,
    failed = false,
    source = source
) {

    val fee = TransactionValue.CoinValue(feeToken, BinanceAdapter.transferFee)
    val memo = transaction.memo

}
