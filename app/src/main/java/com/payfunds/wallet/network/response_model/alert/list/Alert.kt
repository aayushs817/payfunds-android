package com.payfunds.wallet.network.response_model.alert.list

data class Alert(
    val __v: Int,
    val _account: String,
    val _id: String,
    val createdAt: String,
    val frequency: String,
    val price: String,
    val shouldAlert: Boolean,
    val status: String,
    val token: Token,
    val type: String,
    val updatedAt: String,
    val walletAddress: String,
    val isSelected: Boolean
)