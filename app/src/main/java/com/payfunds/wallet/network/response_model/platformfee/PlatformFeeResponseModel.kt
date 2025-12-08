package com.payfunds.wallet.network.response_model.platformfee

data class PlatformFeeResponseModel(
    val `data`: List<Data>,
    val msg: String,
    val result: Int
)