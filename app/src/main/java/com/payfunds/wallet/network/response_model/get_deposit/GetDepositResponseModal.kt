package com.payfunds.wallet.network.response_model.get_deposit

data class GetDepositResponseModal(
    val `data`: Data,
    val message: String,
    val success: Boolean
)