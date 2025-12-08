package com.payfunds.wallet.network.response_model.get_tokens

data class GetTokensResponseModel(
    val `data`: List<CoinDataModel>,
    val isSuccess: Boolean,
    val message: String,
    val statusCode: Int,
    val totalPage: Int
)