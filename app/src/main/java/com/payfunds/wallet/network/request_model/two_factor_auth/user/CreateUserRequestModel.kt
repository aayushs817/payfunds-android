package com.payfunds.wallet.network.request_model.two_factor_auth.user

data class CreateUserRequestModel(
    val walletAddress: String,
    val wallets: List<Wallet>,
    val referralCode: String? = null
)