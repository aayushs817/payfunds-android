package com.payfunds.wallet.network.request_model.create_payment

data class CreatePaymentRequestModel(
    val amount: String,
    val currency: String,
    val token: Token,
    val walletAddress: String
)