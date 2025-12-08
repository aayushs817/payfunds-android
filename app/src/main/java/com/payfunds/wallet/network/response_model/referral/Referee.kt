package com.payfunds.wallet.network.response_model.referral

data class Referee(
    val _id: String,
    val referee: RefereeX,
    val totalInvestment: TotalInvestment,
    val updatedAt: String
)