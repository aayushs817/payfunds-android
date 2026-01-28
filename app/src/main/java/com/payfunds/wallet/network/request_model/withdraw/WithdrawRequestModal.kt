package com.payfunds.wallet.network.request_model.withdraw

data class WithdrawRequestModal(
    val amount: String,
    val walletAddress: String
)