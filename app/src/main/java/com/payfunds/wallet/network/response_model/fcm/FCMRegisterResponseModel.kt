package com.payfunds.wallet.network.response_model.fcm

data class FCMRegisterResponseModel(
    val `data`: Data,
    val msg: String,
    val result: Int
)