package com.payfunds.wallet.network.response_model.can_card_request

data class CanCardRequestModal(
    val `data`: Data,
    val isSuccess: Boolean,
    val message: String,
    val statusCode: Int,
    val totalPage: Any
)