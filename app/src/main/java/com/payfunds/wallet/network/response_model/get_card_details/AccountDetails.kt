package com.payfunds.wallet.network.response_model.get_card_details

data class AccountDetails(
    val accountDisplayDetails: AccountDisplayDetails,
    val transferFunctions: TransferFunctions
)