package com.payfunds.wallet.network.response_model.get_all_notification

data class GetAllNotificationResponseModel(
    val `data`: List<Data>,
    val isSuccess: Boolean,
    val itemsPerPage: Int,
    val message: String,
    val startIndex: Int,
    val statusCode: Int,
    val totalItems: Int,
    val totalPage: Int
)