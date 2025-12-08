package com.payfunds.wallet.network.response_model.referral

data class Data(
    val refereeList: List<Referee>,
    val refereeWithThresholdInvestment: Int,
    val referralCode: String,
    val totalReferee: Int
)