package com.payfunds.wallet.network.response_model.get_card_details

data class CardX(
    val bankAccount: BankAccount,
    val bankAccountId: String,
    val cardType: String,
    val createdAt: String,
    val deletedAt: Any,
    val hasPin: Boolean,
    val id: String,
    val labelName: Any,
    val product: Product,
    val productId: String,
    val refDetails: RefDetails,
    val status: String,
    val statusReason: Any,
    val updatedAt: String
)