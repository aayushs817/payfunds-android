package com.payfunds.wallet.network.response_model.transaction_merged


data class Item(
    val decoded_input: DecodedInput?,
    val from: From?,
    val hash: String?,
    val method: String?,
    val timestamp: String?,
    val to: To? = null,
    val value: String?,
    val token: Token?,
    val total: Total?,
    val tx_hash: String?,
)