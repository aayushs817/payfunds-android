package com.payfunds.wallet.network.response_model.get_card_3ds

data class GetCard3dsForwardingResponseModal(
    val `data`: Data,
    val message: String,
    val success: Boolean
)