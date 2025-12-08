package com.payfunds.wallet.modules.multiswap.providers.tron.trigger_smart_contract_response

data class RawData(
    val contract: List<Contract>,
    val expiration: Long,
    val fee_limit: Int,
    val ref_block_bytes: String,
    val ref_block_hash: String,
    val timestamp: Long
)