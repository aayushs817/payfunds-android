package com.payfunds.wallet.network.response_model.get_card_details

data class FeesTemplate(
    val background_image: String,
    val createdAt: String,
    val displayName: String,
    val id: String,
    val image: String,
    val isActive: Boolean,
    val isStandard: Boolean,
    val movedToTrash: Boolean,
    val name: String,
    val order: Int,
    val ownerId: String,
    val simpleTemplates: List<SimpleTemplate>,
    val templates: List<TemplateX>,
    val type: String,
    val updatedAt: String
)