package com.payfunds.wallet.network.response_model.get_card_details

data class DEPOSIT(
    val entities: List<Entity>,
    val operationalfee: Operationalfee
)