package com.payfunds.wallet.modules.multiswap.providers.tron.trigger_smart_contract_response

data class TriggerSmartContractResponse(
    val constant_result: List<String>,
    val energy_used: Int,
    val internal_transactions: List<InternalTransaction>,
    val result: Result,
    val transaction: Transaction
)