package com.payfunds.wallet.network.response_model.get_card_info

data class GetCardInfoResponseModal(
    val `data`: Data,
    val isSuccess: Boolean,
    val message: String,
    val statusCode: Int,
    val totalPage: Any
)