package com.payfunds.wallet.network.response_model.get_card_details

data class SimpleTemplate(
    val createdAt: String,
    val displayName: String,
    val id: String,
    val isActive: Boolean,
    val movedToTrash: Boolean,
    val name: String,
    val planCategory: String,
    val providerId: String,
    val template: Template,
    val updatedAt: String
)