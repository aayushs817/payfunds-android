package com.payfunds.wallet.modules.alert


import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.payfunds.wallet.network.PayFundWebSocketHandler
import com.payfunds.wallet.network.request_model.coinapi.CoinAPIRequestModel
import com.payfunds.wallet.network.response_model.coinapi.CoinAPIResponseModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class CoinAPIViewModel : ViewModel() {

    private val webSocketHandler = PayFundWebSocketHandler

    val priceAndSymboleMap = mutableStateMapOf<String, MutableStateFlow<CoinAPIResponseModel?>>()
    val priceGet = mutableStateOf<CoinAPIResponseModel?>(null)
    var apiError = mutableStateOf<String?>(null)
    var isApiLoading = mutableStateOf(false)

    fun getStateForToken(symbol: String): StateFlow<CoinAPIResponseModel?> {
        return priceAndSymboleMap.getOrPut(symbol) { MutableStateFlow(null) }
    }

    fun sendCoinAPIRequests(coinAPIRequestModel: CoinAPIRequestModel) {
        isApiLoading.value = true
        try {
            val requestBody = Gson().toJson(coinAPIRequestModel)
            webSocketHandler.sendMessage(requestBody)
            webSocketHandler.connect(
                onMessage = { message ->
                    try {
                        val response = Gson().fromJson(message, CoinAPIResponseModel::class.java)
                        if (response.time_coinapi.isNotEmpty()) {
                            val symbol = response.symbol_id
                                .replace("BINANCE_SPOT_", "")
                                .replace("_USDT", "")

                            priceAndSymboleMap[symbol]?.value = response
                        }
                    } catch (e: Exception) {
                        apiError.value = "Error parsing response: ${e.message}"
                    }
                    isApiLoading.value = false
                },
                onError = { error ->
                    apiError.value = "Error: $error"
                    isApiLoading.value = false
                }
            )
        } catch (e: Exception) {
            apiError.value = e.message
            isApiLoading.value = false
            e.printStackTrace()
        }
    }

    fun disconnectWebSocket() {
        try {
            isApiLoading.value = true
            webSocketHandler.disconnect()
            isApiLoading.value = false
        } catch (e: Exception) {
            apiError.value = e.message
            isApiLoading.value = false
            e.printStackTrace()
        }
    }


    fun sendCoinAPIRequest(coinAPIRequestModel: CoinAPIRequestModel) {
        isApiLoading.value = true
        val requestBody = Gson().toJson(coinAPIRequestModel)

        PayFundWebSocketHandler.connect(
            onMessage = { message ->
                try {
                    val response = Gson().fromJson(message, CoinAPIResponseModel::class.java)
                    if (response.time_coinapi.isNotEmpty()) {
                        priceGet.value = response
                    }
                } catch (e: Exception) {
                    apiError.value = "Error parsing response: ${e.message}"
                }
                isApiLoading.value = false
            },
            onError = { error ->
                apiError.value = "Error: $error"
                isApiLoading.value = false
            }
        )
        PayFundWebSocketHandler.sendMessage(requestBody)
    }

}
