package com.payfunds.wallet.network.response_model.get_bank_account

data class GetBankAccountResponseModal(
    val `data`: List<Data>,
    val message: String,
    val success: Boolean
)