package com.payfunds.wallet.network.response_model.nominee.update

data class UpdateNomineeResponseModel(
    val `data`: Data,
    val msg: String,
    val result: Int
)