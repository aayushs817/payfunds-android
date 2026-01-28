package com.payfunds.wallet.network.response_model.get_card_details

data class RefDetails(
    val availableCredit: String,
    val cardDesign: String,
    val cardName: String,
    val cardType: String,
    val last4: String,
    val meta: Meta,
    val physicalCardStatus: String,
    val secondaryCardName: Any,
    val shippingAddress: Any,
    val shippingInformation: ShippingInformation,
    val spendControl: SpendControl,
    val status: String,
    val threeDSForwarding: Boolean
)