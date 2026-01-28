package com.payfunds.wallet.network.response_model.get_user_details

data class Data(
    val _id: String,
    val bankDetails: BankDetails,
    val countryCode: String,
    val createdAt: String,
    val email: String,
    val fullName: String,
    val isEmailVerified: Boolean,
    val isVerified: Boolean,
    val mobile: String,
    val profilePic: String,
    val updatedAt: String,
    val username: String,
    val wallets: List<Wallet>
)