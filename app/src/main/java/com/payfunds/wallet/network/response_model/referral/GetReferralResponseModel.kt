package com.payfunds.wallet.network.response_model.referral

data class GetReferralResponseModel(
    val `data`: Data,
    val msg: String,
    val result: Int
)