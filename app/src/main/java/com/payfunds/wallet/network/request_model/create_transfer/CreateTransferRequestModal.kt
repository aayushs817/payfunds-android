package com.payfunds.wallet.network.request_model.create_transfer

data class CreateTransferRequestModal(
    val amount: Int,
    val currency: String,
    val fromAccountId: String,
    val toAccountId: String
)