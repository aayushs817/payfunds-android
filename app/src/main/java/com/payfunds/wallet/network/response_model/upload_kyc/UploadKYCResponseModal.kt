package com.payfunds.wallet.network.response_model.upload_kyc

data class UploadKYCResponseModal(
    val `data`: Data,
    val isSuccess: Boolean,
    val message: String,
    val statusCode: Int,
    val totalPage: Any
)