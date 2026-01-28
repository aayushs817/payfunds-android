package com.payfunds.wallet.network.response_model.get_card_details

data class AtmControl(
    val dailyFrequency: String,
    val dailyWithdrawal: String,
    val monthlyFrequency: String,
    val monthlyWithdrawal: String,
    val yearlyFrequency: String,
    val yearlyWithdrawal: String
)