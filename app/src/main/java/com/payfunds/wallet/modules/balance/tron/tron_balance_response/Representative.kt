package com.payfunds.wallet.modules.balance.tron.tron_balance_response

data class Representative(
    val allowance: Int,
    val enabled: Boolean,
    val lastWithDrawTime: Int,
    val url: String
)