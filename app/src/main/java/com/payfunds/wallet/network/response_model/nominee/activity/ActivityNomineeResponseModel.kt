package com.payfunds.wallet.network.response_model.nominee.activity

data class ActivityNomineeResponseModel(
    val `data`: Data,
    val msg: String,
    val result: Int
)