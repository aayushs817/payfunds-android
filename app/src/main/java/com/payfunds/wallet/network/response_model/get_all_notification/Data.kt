package com.payfunds.wallet.network.response_model.get_all_notification

data class Data(
    val _account: String,
    val _id: String,
    val description: String,
    val isRead: Boolean,
    val metadata: Metadata,
    val readAt: String? = null,
    val title: String,
    val createdAt : String? = null
)