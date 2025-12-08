package com.payfunds.wallet.network.response_model.transaction

data class Parameter(
    val name: String,
    val type: String,
    val value: String
) {
    fun toMergedParameter(): Parameter {
        return Parameter(
            name = name,
            type = type,
            value = value
        )
    }
}