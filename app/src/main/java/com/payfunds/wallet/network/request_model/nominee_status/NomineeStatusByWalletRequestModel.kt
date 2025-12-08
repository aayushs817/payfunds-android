package com.payfunds.wallet.network.request_model.nominee_status

data class NomineeStatusByWalletRequestModel(
    val walletAddresses: List<String>
)