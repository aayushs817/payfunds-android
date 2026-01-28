package com.payfunds.wallet.network.response_model.get_card_transactions

data class Data(
    val pagination: Pagination,
    val transactions: List<Transaction>
)