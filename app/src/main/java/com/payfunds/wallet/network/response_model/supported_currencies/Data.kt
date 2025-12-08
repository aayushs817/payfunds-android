package com.payfunds.wallet.network.response_model.supported_currencies

data class Data(
    val code: String,
    val decimal_places: Int,
    val name: String,
    val symbol: String,
    val flag: String
)