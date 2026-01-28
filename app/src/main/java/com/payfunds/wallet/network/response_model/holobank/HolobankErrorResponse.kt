package com.payfunds.wallet.network.response_model.holobank

import com.google.gson.annotations.SerializedName

data class HolobankErrorResponse(
    @SerializedName("isSuccess")
    val success: Boolean,
    val message: String,
    val statusCode: Int? = null,
    val error: String? = null
)
