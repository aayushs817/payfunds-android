package com.payfunds.wallet.network.response_model.radeemed_gift_card

data class Data(
    val __v: Int,
    val _id: String,
    val code: String,
    val createdAt: String,
    val details: Details,
    val isRedeemed: Boolean,
    val payment: Payment,
    val `receiver`: Receiver,
    val sendType: String,
    val sender: Sender,
    val status: String,
    val thumbnail: String,
    val token: Token,
    val updatedAt: String
)