package com.payfunds.wallet.network.request_model.fcm

data class FCMRegisterRequestModel(
    val deviceId: String,
    val fcmToken: String,
    val os: String,
    val walletAddress: String
)