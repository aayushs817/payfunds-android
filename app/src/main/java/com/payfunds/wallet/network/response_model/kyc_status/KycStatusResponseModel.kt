package com.payfunds.wallet.network.response_model.kyc_status

data class KycStatusResponseModel(
    val `data`: Data,
    val msg: String,
    val result: Int
)