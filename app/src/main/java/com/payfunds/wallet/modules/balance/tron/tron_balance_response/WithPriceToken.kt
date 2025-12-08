package com.payfunds.wallet.modules.balance.tron.tron_balance_response

data class WithPriceToken(
    val amount: Double,
    val balance: String,
    val nrOfTokenHolders: Long,
    val tokenAbbr: String,
    val tokenCanShow: Long,
    val tokenDecimal: Long,
    val tokenId: String,
    val tokenLogo: String,
    val tokenName: String,
    val tokenPriceInTrx: Double,
    val tokenType: String,
    val transferCount: Long,
    val vip: Boolean
)