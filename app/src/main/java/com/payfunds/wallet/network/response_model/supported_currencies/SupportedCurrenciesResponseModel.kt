package com.payfunds.wallet.network.response_model.supported_currencies

data class SupportedCurrenciesResponseModel(
    val `data`: List<Data>,
    val msg: String,
    val result: Int
)