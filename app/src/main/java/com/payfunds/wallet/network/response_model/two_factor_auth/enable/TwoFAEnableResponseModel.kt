package com.payfunds.wallet.network.response_model.two_factor_auth.enable

data class TwoFAEnableResponseModel(
    val error: Any,
    val isSuccess: Boolean,
    val message: String,
    val statusCode: Int
)