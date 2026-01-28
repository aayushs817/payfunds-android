package com.payfunds.wallet.network.response_model.get_card_details

data class MinimumBalance(
    val offshoreBankAccount: OffshoreBankAccount,
    val virtualBankAccount: VirtualBankAccount,
    val walletsAccount: WalletsAccount
)