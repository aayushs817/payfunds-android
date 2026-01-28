package com.payfunds.wallet.network.response_model.get_user_details

data class GetUserDetailsResponseModal(
    val `data`: Data,
    val message: String,
    val success: Boolean
)