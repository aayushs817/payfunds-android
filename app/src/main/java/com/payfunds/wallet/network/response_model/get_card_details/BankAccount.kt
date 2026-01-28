package com.payfunds.wallet.network.response_model.get_card_details

data class BankAccount(
    val bankAccountStatus: String,
    val bankAccountType: String,
    val bankProviderId: String,
    val coreUserId: String,
    val createdAt: String,
    val currency: Currency,
    val currencyId: String,
    val deletedAt: Any,
    val id: String,
    val productId: String,
    val purpose: String,
    val statusReason: Any,
    val updatedAt: String
)