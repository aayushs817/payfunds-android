package com.payfunds.wallet.network.response_model.get_user_details

data class BankDetails(
    val accounts: List<Account>,
    val cards: List<Card>,
    val holobankUserId: String,
    val kycId: String,
    val kycStatus: String,
    val updateReason: String? = null,
    val rejectionReason: String? = null,
    val cardRequests: Boolean = false,
    val userReferenceId: String
)