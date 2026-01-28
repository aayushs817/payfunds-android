package com.payfunds.wallet.network.response_model.get_card_details

data class ProviderX(
    val createdAt: String,
    val friendlyName: String,
    val id: String,
    val isCore: Boolean,
    val isDefault: Boolean,
    val masterCoreUserId: String,
    val metadata: MetadataX,
    val updatedAt: String,
    val userKeyPrefix: String
)