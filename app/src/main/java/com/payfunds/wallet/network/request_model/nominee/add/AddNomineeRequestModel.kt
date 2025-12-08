package com.payfunds.wallet.network.request_model.nominee.add

data class AddNomineeRequestModel(
    val nominatorEmail: String,
    val email: String,
    val name: String,
    val relation: String,
    val walletAddress: String
)