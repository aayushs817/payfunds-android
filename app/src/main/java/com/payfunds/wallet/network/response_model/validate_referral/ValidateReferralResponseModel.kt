package com.payfunds.wallet.network.response_model.validate_referral

data class ValidateReferralResponseModel(
    val `data`: Data,
    val msg: String,
    val result: Int
)