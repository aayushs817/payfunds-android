package com.payfunds.wallet.network.request_model.create_core_user

data class CreateCoreUserRequestModal(
    val firstName: String,
    val lastName: String,
    val displayName: String,
    val email: String,
    val phoneNumber: String,
    val type: String
)