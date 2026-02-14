package com.payfunds.wallet.network.response_model.can_card_request

data class DepositInfo(
    val accountId: String,
    val address: String,
    val balance: Int,
    val chainName: String,
    val symbol: String,
    val type: String
)