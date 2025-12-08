package com.payfunds.wallet.network.request_model.send_gift_card

data class SendGiftCardRequestModel(
    val details: Details,
    val payment: Payment,
    val receiver: Receiver,
    val sendType: String,
    val sender: Sender,
    val thumbnail: String,
    val token: Token,
    val scheduledAt: String? = null
)