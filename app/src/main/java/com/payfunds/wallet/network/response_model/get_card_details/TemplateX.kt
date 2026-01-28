package com.payfunds.wallet.network.response_model.get_card_details

data class TemplateX(
    val createdAt: String,
    val extendedFeesTemplate: ExtendedFeesTemplate,
    val id: String,
    val planCategory: String,
    val updatedAt: String
)