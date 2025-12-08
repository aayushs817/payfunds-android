package com.payfunds.wallet.network.response_model.two_factor_auth.check_wallet_address

data class TwoFactorAuthCheckWalletAddressResponseModel(
    val `data`: List<Data>,
    val isSuccess: Boolean,
    val message: String,
    val statusCode: Int,
    val totalPage: Any
)