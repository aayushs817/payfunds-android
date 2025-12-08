package com.payfunds.wallet.network.response_model.request_kyc

data class RequestKycResponseModel(
    val `data`: Data,
    val msg: String,
    val result: Int
)