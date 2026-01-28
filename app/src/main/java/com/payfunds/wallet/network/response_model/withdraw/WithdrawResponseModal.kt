package com.payfunds.wallet.network.response_model.withdraw

data class WithdrawResponseModal(
    val `data`: Data,
    val isSuccess: Boolean,
    val message: String,
    val statusCode: Int,
    val totalPage: Any
)