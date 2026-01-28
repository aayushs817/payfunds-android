package com.payfunds.wallet.network.response_model.get_card_details

data class Kyb(
    val offshoreCompleted: Boolean,
    val platformCompleted: Boolean,
    val virtualCompleted: Boolean
)