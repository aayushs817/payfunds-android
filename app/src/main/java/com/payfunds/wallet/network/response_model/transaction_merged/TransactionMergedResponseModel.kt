package com.payfunds.wallet.network.response_model.transaction_merged

data class TransactionMergedResponseModel(
    val items: List<Item>,
    val next_page_params: Any
)