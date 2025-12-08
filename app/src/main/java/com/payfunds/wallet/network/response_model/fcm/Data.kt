package com.payfunds.wallet.network.response_model.fcm

data class Data(
    val __v: Int,
    val _account: String,
    val _id: String,
    val createdAt: String,
    val deviceId: String,
    val fcmToken: String,
    val os: String,
    val updatedAt: String,
    val walletAddress: String
)