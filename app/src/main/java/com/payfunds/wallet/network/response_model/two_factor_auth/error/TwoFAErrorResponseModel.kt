package com.payfunds.wallet.network.response_model.two_factor_auth.error

data class TwoFAErrorResponseModel(
    val error: Any,
    val isSuccess: Boolean,
    val message: String,
    val statusCode: Int
)