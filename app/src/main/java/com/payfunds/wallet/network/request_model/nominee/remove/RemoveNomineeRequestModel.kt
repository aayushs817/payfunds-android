package com.payfunds.wallet.network.request_model.nominee.remove

data class RemoveNomineeRequestModel(
    val nomineeId: String,
    val walletAddress: String
)