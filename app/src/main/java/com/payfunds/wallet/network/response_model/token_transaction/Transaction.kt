package com.payfunds.wallet.network.response_model.token_transaction

data class Transaction(
    val __v: Int,
    val _account: String,
    val _id: String,
    val amount: String,
    val createdAt: String,
    val currency: String,
    val customerId: String,
    val paymentId: String,
    val paymentIdClientSecret: String,
    val status: String,
    val token: Token,
    val updatedAt: String,
    val walletAddress: String
)