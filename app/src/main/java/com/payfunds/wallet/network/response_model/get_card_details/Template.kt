package com.payfunds.wallet.network.response_model.get_card_details

data class Template(
    val DEPOSIT: List<DEPOSIT>,
    val WITHDRAW: List<WITHDRAW>
)