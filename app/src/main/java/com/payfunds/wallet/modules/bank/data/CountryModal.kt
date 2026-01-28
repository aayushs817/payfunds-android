package com.payfunds.wallet.modules.bank.data

data class CountryModal(
    val name: String,
    val code: String,
    val flagRes: Int,
    val isoCode: String // New field for identification (e.g., "US")
)