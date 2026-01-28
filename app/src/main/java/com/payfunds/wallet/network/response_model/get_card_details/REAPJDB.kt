package com.payfunds.wallet.network.response_model.get_card_details

data class REAPJDB(
    val isDeleted: Boolean,
    val status: Boolean,
    val value: Double,
    val valueType: String
)