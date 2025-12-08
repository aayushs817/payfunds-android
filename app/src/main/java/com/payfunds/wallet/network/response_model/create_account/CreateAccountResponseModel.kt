package com.payfunds.wallet.network.response_model.create_account

data class CreateAccountResponseModel(
    val `data`: Data,
    val msg: String,
    val result: Int
)