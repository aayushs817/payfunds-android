package com.payfunds.wallet.network.response_model.alert.list

import com.payfunds.wallet.network.response_model.alert.list.grouped.GroupAlert
import com.payfunds.wallet.network.response_model.alert.list.grouped.TokenAlert

data class ListAlertResponseModel(
    val isSuccess: Boolean,
    val result: Int,
    val msg: String,
    val data: Data
)


fun groupAlertsByToken(alerts: List<Alert>): List<TokenAlert> {
    return alerts.groupBy { it.token }
        .map { (token, alerts) ->
            TokenAlert(
                name = token.name,
                symbol = token.symbol,
                _id = token.hashCode().toString(),
                alerts = alerts.map { alert ->
                    GroupAlert(
                        type = alert.type,
                        price = alert.price,
                        frequency = alert.frequency,
                        shouldAlert = alert.shouldAlert,
                        status = alert.status,
                        createdAt = alert.createdAt,
                        updatedAt = alert.updatedAt,
                        __v = alert.__v,
                        _account = alert._account,
                        _id = alert._id,
                        token = alert.token,
                        walletAddress = alert.walletAddress,
                    )
                }
            )
        }
}
