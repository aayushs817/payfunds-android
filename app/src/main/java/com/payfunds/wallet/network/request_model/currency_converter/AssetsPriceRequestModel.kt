package com.payfunds.wallet.network.request_model.currency_converter

data class AssetsPriceRequestModel(
    val coins: List<String>,
    val currency: String
)