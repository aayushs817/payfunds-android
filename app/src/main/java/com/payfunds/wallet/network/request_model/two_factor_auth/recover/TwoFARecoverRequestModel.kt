package com.payfunds.wallet.network.request_model.two_factor_auth.recover

data class TwoFARecoverRequestModel(
    val walletAddress: String,
    val recoveryCode: String
)