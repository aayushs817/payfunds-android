package com.payfunds.wallet.network.response_model.get_profile

data class Document(
    val age: Int,
    val country: String,
    val dob: String,
    val document_number: String,
    val expiry_date: String,
    val face_match_confidence: Int,
    val full_address: String,
    val gender: Any,
    val issue_date: String,
    val name: Name,
    val selected_type: List<String>,
    val supported_types: List<String>
)