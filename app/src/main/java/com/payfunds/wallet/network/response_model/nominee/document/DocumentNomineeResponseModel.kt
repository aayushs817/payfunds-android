package com.payfunds.wallet.network.response_model.nominee.document

data class DocumentNomineeResponseModel(
    val `data`: Data,
    val msg: String,
    val result: Int
)