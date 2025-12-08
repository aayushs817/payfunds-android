package com.payfunds.wallet.network.request_model.check_wallet_address

data class TwoFactorAuthCheckWalletAddressRequestModel(
    val walletAddresses: List<String>
)