package com.payfunds.wallet.network.response_model.nominee.remove

data class RemoveNomineeResponseModel(
    val `data`: Data,
    val msg: String,
    val result: Int
)