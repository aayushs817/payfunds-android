package com.payfunds.wallet.network.response_model.get_card_details

data class Metadata(
    val background_image: String,
    val capabilities: List<String>,
    val card_design: String,
    val category: String,
    val color: Color,
    val features: List<Any>,
    val image: String,
    val name: String,
    val picture: Picture,
    val themeMode: String
)