package com.payfunds.wallet.network.request_model.notification_mark_as_read

data class NotificationMarkAsReadRequestModel(
    val _notification: String? = null,
    val isMarkAsAllRead: Boolean? = null
)