package com.payfunds.wallet.network.response_model.get_card_details

data class Token(
    val abi: List<String>,
    val address: String,
    val chainId: String,
    val confirmations: Int,
    val createdAt: String,
    val decimals: Int,
    val id: String,
    val isNative: Boolean,
    val isWatchable: Boolean,
    val name: String,
    val symbol: String,
    val updatedAt: String
)