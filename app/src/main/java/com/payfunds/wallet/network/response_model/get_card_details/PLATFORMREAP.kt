package com.payfunds.wallet.network.response_model.get_card_details

data class PLATFORMREAP(
    val isDeleted: Boolean,
    val status: Boolean,
    val value: Double,
    val valueType: String
)