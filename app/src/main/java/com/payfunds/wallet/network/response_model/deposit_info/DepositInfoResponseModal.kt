package com.payfunds.wallet.network.response_model.deposit_info

data class DepositInfoResponseModal(
    val `data`: Data,
    val isSuccess: Boolean,
    val message: String,
    val statusCode: Int,
    val totalPage: Any
)