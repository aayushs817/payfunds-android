package com.payfunds.wallet.modules.multiswap.sendtransaction

import io.horizontalsystems.ethereumkit.models.FullTransaction

sealed class SendTransactionResult {
    data class Evm(val fullTransaction: FullTransaction) : SendTransactionResult()
    data class Tron(
        val txId: String,
        val fromAddress: String,
        val toAddress: String,
        val feeCoinCode: String
    ) : SendTransactionResult()
}
