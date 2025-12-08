package com.payfunds.wallet.modules.multiswap.providers.tron.trigger_smart_contract_response

data class Value(
    val contract_address: String,
    val `data`: String,
    val owner_address: String
)