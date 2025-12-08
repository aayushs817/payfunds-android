package com.payfunds.wallet.network.response_model.nominee_status

data class NomineeStatusByWalletResponseModel(
    val `data`: List<Data>,
    val msg: String,
    val result: Int
)