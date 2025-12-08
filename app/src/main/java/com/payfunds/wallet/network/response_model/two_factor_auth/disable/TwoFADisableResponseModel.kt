package com.payfunds.wallet.network.response_model.two_factor_auth.disable

data class TwoFADisableResponseModel(
    val error: Any,
    val isSuccess: Boolean,
    val message: String,
    val statusCode: Int
)