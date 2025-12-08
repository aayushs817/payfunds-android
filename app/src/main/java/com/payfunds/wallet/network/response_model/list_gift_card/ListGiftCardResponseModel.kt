package com.payfunds.wallet.network.response_model.list_gift_card

data class ListGiftCardResponseModel(
    val `data`: List<Data>,
    val msg: String,
    val result: Int
)