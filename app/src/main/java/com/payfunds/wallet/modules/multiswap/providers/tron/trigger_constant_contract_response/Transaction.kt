package com.payfunds.wallet.modules.multiswap.providers.tron.trigger_constant_contract_response

data class Transaction(
    val raw_data: RawData,
    val raw_data_hex: String,
    val ret: List<Ret>,
    val txID: String,
    val visible: Boolean
)