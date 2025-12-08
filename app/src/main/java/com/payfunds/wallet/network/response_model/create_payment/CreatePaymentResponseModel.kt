package com.payfunds.wallet.network.response_model.create_payment

data class CreatePaymentResponseModel(
    val `data`: Data,
    val msg: String,
    val result: Int
)