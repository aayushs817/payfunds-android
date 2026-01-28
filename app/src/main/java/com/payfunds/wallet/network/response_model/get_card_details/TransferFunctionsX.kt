package com.payfunds.wallet.network.response_model.get_card_details

data class TransferFunctionsX(
    val directAccount: Boolean,
    val intermediaryBank: Boolean
)