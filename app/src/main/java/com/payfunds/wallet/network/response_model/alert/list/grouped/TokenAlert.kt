package com.payfunds.wallet.network.response_model.alert.list.grouped

data class TokenAlert(
    val _id: String,
    val name: String,
    val symbol: String,
    val alerts: List<GroupAlert>,
)