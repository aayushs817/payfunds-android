package com.payfunds.wallet.network.response_model.get_card_details

data class OtpPhoneNumber(
    val dialCode: Int,
    val phoneNumber: String
)