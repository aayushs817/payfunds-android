package com.payfunds.wallet.network.request_model.send_transaction_details

data class SendTransactionDetailsRequestModel(
    val contractAddress: String? = null,
    val fromAddress: String,
    val symbol: String,
    val toAddress: String,
    val totalAmount: Double,
    val txnHash: String,
    val fromContract: String? = null,
    val toContract: String? = null,
    val type: String? = null
)