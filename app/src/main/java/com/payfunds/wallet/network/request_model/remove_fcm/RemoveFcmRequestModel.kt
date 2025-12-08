package com.payfunds.wallet.network.request_model.remove_fcm

data class RemoveFcmRequestModel(
    val deviceId: String,
    val os: String,
    val walletAddress: String
)