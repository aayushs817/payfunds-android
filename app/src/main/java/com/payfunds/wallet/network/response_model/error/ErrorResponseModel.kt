package com.payfunds.wallet.network.response_model.error

data class ErrorResponseModel(
    val error: Any?,
    val isSuccess: Boolean,
    val message: String,
    val statusCode: Int
)