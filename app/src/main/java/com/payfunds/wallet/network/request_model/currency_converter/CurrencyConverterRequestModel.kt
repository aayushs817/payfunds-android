package com.payfunds.wallet.network.request_model.currency_converter

data class CurrencyConverterRequestModel(
    val amount: Double,
    val baseCurrency: String,
    val targetCurrency: String
)