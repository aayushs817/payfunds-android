package com.payfunds.wallet.network.response_model.get_card_details

data class SpendControl(
    val atmControl: AtmControl,
    val spendControlAmount: SpendControlAmount,
    val spendControlCap: SpendControlCap
)