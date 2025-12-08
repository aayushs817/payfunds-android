package com.payfunds.wallet.network.response_model.get_profile

data class GetProfileResponseModel(
    val `data`: Data,
    val msg: String,
    val result: Int
)