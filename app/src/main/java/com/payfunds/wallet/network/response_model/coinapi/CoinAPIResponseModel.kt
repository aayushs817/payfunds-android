package com.payfunds.wallet.network.response_model.coinapi

data class CoinAPIResponseModel(
    val price: Double,
    val sequence: Int,
    val size: Double,
    val symbol_id: String,
    val taker_side: String,
    val time_coinapi: String,
    val time_exchange: String,
    val type: String,
    val uuid: String
)
