package com.payfunds.wallet.network.response_model.transaction

data class TransactionResponseModel(
    val items: List<Item>,
    val next_page_params: Any
)