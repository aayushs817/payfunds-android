package com.payfunds.wallet.network.request_model.redeemed_gift_card

data class RedeemedGiftCardRequestModel(
    val giftCode: String? = null,
    val giftId: String? = null,
    val walletAddress: String
)