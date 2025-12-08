package com.payfunds.wallet.network.response_model.get_profile

data class TransferToNominee(
    val acceptancePeriodStartDate: String,
    val acceptanceStatus: String,
    val acceptanceVerificationCode: String,
    val kyc: Kyc,
    val kycStatus: String,
    val nomineeAcceptanceStatusStack: List<Any?>,
    val nomineeId: String,
    val nomineeIndex: Int
)