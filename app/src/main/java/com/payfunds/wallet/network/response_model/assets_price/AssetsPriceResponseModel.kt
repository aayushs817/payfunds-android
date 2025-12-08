package com.payfunds.wallet.network.response_model.assets_price

data class AssetsPriceResponseModel(
    val `data`: List<Data>,
    val msg: String,
    val result: Int
)