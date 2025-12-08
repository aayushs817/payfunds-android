package com.payfunds.wallet.network.response_model.two_factor_auth.verify

data class MultiFactor(
    val googleAuthenticator: GoogleAuthenticator,
    val isEnable: Boolean,
    val recoveryCode: String
)