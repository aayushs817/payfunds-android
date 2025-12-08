package com.payfunds.wallet.network.response_model.assets_price

data class Data(
    val name: String,
    val percentageChange: Double,
    val price: Double,
    val symbol: String
)