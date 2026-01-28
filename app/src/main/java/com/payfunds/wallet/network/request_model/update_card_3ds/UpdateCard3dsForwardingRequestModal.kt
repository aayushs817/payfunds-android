package com.payfunds.wallet.network.request_model.update_card_3ds

data class UpdateCard3dsForwardingRequestModal(
    val forwardingMethod: String,
    val status: String
)