package com.payfunds.wallet.network.response_model.get_user_details

data class Wallet(
    val address: String,
    val chainId: Int,
    val isPrimary: Boolean,
    val label: String,
    val network: String,
    val tokens: List<Token>
)