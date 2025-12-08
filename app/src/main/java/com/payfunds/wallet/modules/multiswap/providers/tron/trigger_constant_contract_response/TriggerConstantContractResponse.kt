package com.payfunds.wallet.modules.multiswap.providers.tron.trigger_constant_contract_response

data class TriggerConstantContractResponse(
    val constant_result: List<String>,
    val energy_used: Int,
    val result: Result,
    val transaction: Transaction
)