package com.payfunds.wallet.network.response_model.transaction_merged

data class DecodedInput(
    val method_call: String,
    val method_id: String,
    val parameters: List<Parameter>
)