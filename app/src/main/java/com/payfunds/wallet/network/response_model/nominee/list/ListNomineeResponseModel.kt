package com.payfunds.wallet.network.response_model.nominee.list

data class ListNomineeResponseModel(
    val `data`: Data,
    val msg: String,
    val result: Int
)