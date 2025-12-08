package com.payfunds.wallet.modules.multiswap.providers.tron.trigger_constant_contract_response

data class RawData(
    val contract: List<Contract>,
    val expiration: Long,
    val ref_block_bytes: String,
    val ref_block_hash: String,
    val timestamp: Long
)