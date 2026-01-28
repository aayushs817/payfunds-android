package com.payfunds.wallet.network.response_model.update_card_lable

data class UpdateCardLabelResponseModal(
    val `data`: Data,
    val isSuccess: Boolean,
    val message: String,
    val statusCode: Int,
    val totalPage: Any
)