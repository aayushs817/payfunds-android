package com.payfunds.wallet.network.response_model.nominee.recovery

data class RecoveryNomineeResponseModel(
    val `data`: Data,
    val msg: String,
    val result: Int
)