package com.payfunds.wallet.network.request_model.nominee.recovery

data class RecoveryNomineeRequestModel(
    val recoveryFile: String,
    val walletAddress: String,
    val nomineeId: String
)