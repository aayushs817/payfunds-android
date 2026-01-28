package com.payfunds.wallet.network.response_model.get_card_details

data class SpendControlCap(
    val allTimeLimit: String,
    val dailyLimit: String,
    val monthlyLimit: String,
    val transactionLimit: String,
    val weeklyLimit: String,
    val yearlyLimit: String
)