package com.payfunds.wallet.network.request_model.nominee.update

data class UpdateNomineeRequestModel(
    val email: String,
    val name: String,
    val nominatorEmail: String,
    val nomineeId: String,
    val onlyEmail: Boolean,
    val relation: String,
    val walletAddress: String
)