package com.payfunds.wallet.network.response_model.notification_mark_as_read

data class NotificationMarkAsReadResponseModel(
    val isSuccess: Boolean,
    val message: String,
    val statusCode: Int,
    val totalPage: Any
)