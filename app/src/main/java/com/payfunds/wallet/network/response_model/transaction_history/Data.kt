package com.payfunds.wallet.network.response_model.transaction_history

data class Data(
    val totalCount: Int,
    val txnHistory: List<TxnHistory>
)