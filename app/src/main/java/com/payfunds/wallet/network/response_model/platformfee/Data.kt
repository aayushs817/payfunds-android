package com.payfunds.wallet.network.response_model.platformfee

data class Data(
    val isActive: Boolean,
    val name: String,
    val updatedById: String,
    val value: Double
)