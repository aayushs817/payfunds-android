package com.payfunds.wallet.network.request_model.nominee.document

data class DocumentNomineeRequestModel(
    val nomineeId: String,
    val walletAddress: String
)