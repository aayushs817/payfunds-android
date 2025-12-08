package com.payfunds.wallet.network.response_model.get_profile

data class Nominee(
    val _id: String,
    val isRecoveryFileStored: Boolean,
    val kyc: Kyc,
    val status: String
)