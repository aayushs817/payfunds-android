package com.payfunds.wallet.network.response_model.two_factor_auth.qr

data class TwoFAQRCodeResponseModel(
    val `data`: Data,
    val isSuccess: Boolean,
    val message: String,
    val statusCode: Int,
    val totalPage: Any
)