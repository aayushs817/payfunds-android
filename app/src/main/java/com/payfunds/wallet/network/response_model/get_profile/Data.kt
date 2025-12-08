package com.payfunds.wallet.network.response_model.get_profile

data class Data(
    val __v: Int,
    val _id: String,
    val createdAt: String,
    val kyc: Kyc,
    val lastActivityTracked: String,
    val receivedGiftCards: Int,
    val referralCode: String,
    val stripe: Stripe,
    val updatedAt: String,
    val walletAddress: String,
    val activityStatus: String,
    val activityVerificationCode: String,
    val email: String,
    val isNomineeVerified: Boolean,
    val nomineeTransferStatus: String,
    val nominees: List<Nominee>,
    val transferToNominee: TransferToNominee,
)