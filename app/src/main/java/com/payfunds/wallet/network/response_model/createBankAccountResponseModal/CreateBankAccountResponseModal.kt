package com.payfunds.wallet.network.response_model.createBankAccountResponseModal

data class CreateBankAccountResponseModal(
    val `data`: Data,
    val message: String,
    val success: Boolean
)