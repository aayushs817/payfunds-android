package com.payfunds.wallet.network.response_model.get_card_details

data class Kyc(
    val offshoreCompleted: Boolean,
    val platformCompleted: Boolean,
    val virtualCompleted: Boolean
)