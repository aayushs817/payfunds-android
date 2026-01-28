package com.payfunds.wallet.network.response_model.get_card_details

data class SpendControlAmount(
    val allTimeSpent: String,
    val dailySpent: String,
    val monthlySpent: String,
    val weeklySpent: String,
    val yearlySpent: String
)