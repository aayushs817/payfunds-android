package com.payfunds.wallet.network.response_model.nominee_status

data class Nominee(
    val _id: String,
    val isRecoveryFileStored: Boolean,
    val kyc: Kyc,
    val status: String
)