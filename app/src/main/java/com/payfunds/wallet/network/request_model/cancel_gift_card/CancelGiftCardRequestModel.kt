package com.payfunds.wallet.network.request_model.cancel_gift_card

data class CancelGiftCardRequestModel(
    val giftId: String,
    val walletAddress: String
)