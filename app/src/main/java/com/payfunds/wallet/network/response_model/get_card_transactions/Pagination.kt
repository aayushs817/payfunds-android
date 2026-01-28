package com.payfunds.wallet.network.response_model.get_card_transactions

data class Pagination(
    val currentPage: Int,
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean,
    val totalCount: Int,
    val totalPages: Int
)