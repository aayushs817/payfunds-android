package com.payfunds.wallet.network.response_model.nominee.list

data class Nominee(
    val __v: Int,
    val _id: String,
    val createdAt: String,
    val email: String,
    val isRecoveryFileStored: Boolean,
    val kyc: Kyc,
    val name: String,
    val recoveryFile: RecoveryFile,
    val relation: String,
    val status: String,
    val updatedAt: String
)