package com.payfunds.wallet.network.request_model.coinapi

data class CoinAPIRequestModel(
    val heartbeat: Boolean,
    val subscribe_data_type: List<String>,
    val subscribe_filter_asset_id: List<String>,
    val subscribe_filter_exchange_id: List<String>,
    val subscribe_update_limit_ms_quote: Int,
    val type: String
)