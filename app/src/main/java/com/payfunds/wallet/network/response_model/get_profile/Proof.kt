package com.payfunds.wallet.network.response_model.get_profile

data class Proof(
    val address: String,
    val country: String,
    val dob: String,
    val document_country: String,
    val document_country_code: String,
    val document_number: String,
    val document_type: String,
    val expiry_date: String,
    val face: String,
    val first_name: String,
    val full_name: String,
    val issue_date: String,
    val last_name: String
)