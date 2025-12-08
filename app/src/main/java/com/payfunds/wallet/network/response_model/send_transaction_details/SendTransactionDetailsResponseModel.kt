package com.payfunds.wallet.network.response_model.send_transaction_details

data class SendTransactionDetailsResponseModel(
    val _id: String? = null,
    val _user: String? = null,
    val _token: String? = null,
    val totalAmount: Double? = null,
    val txnHash: String? = null,
    val fromAddress: String? = null,
    val toAddress: String? = null,
    val contractAddress: String? = null,
    val symbol: String? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null
)
