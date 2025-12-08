package com.payfunds.wallet.modules.multiswap.providers.tron.trigger_smart_contract_response

data class InternalTransaction(
    val callValueInfo: List<CallValueInfo>,
    val caller_address: String,
    val hash: String,
    val note: String,
    val transferTo_address: String
)