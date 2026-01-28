package com.payfunds.wallet.network.response_model.create_core_user

data class Data(
    val holobankUserId: String,
    val message: String,
    val referenceId: String
)