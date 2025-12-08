package com.payfunds.wallet.network.request_model.list_gift_card

data class ListGiftCardRequestModel(
    val status: String,
    val type: String,
    val walletAddress: String
)