package com.payfunds.wallet.network.response_model.create_core_user

import com.google.gson.annotations.SerializedName


data class CreateCoreUserResponseModal(
    val `data`: Data,
    val message: String,
    @SerializedName("isSuccess")
    val success: Boolean

)