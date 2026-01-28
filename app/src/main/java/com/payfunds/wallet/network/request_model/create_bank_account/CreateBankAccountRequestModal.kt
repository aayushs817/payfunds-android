package com.payfunds.wallet.network.request_model.create_bank_account

data class CreateBankAccountRequestModal(
    val currency: String,
    val type: String
)