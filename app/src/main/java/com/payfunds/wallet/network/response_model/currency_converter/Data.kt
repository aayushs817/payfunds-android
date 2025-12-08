package com.payfunds.wallet.network.response_model.currency_converter

data class Data(
    val amount: Double,
    val baseCurrency: String,
    val conversion: Double,
    val targetCurrency: String
)