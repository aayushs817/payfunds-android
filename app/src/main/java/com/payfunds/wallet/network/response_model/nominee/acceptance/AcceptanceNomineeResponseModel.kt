package com.payfunds.wallet.network.response_model.nominee.acceptance

data class AcceptanceNomineeResponseModel(
    val `data`: Data,
    val msg: String,
    val result: Int
)