package com.payfunds.wallet.modules.alert

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.payfunds.wallet.network.PayFundRetrofitInstance
import com.payfunds.wallet.network.request_model.alert.cancel.CancelAlertRequestModel
import com.payfunds.wallet.network.request_model.alert.create.CreateAlertRequestModel
import com.payfunds.wallet.network.request_model.alert.create.Token
import com.payfunds.wallet.network.request_model.alert.list.ListAlertRequestModel
import com.payfunds.wallet.network.response_model.alert.cancel.CancelAlertResponseModel
import com.payfunds.wallet.network.response_model.alert.create.CreateAlertResponseModel
import com.payfunds.wallet.network.response_model.alert.list.ListAlertResponseModel
import com.payfunds.wallet.network.response_model.error.ErrorResponseModel
import io.reactivex.internal.util.NotificationLite.getError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlertViewModel : ViewModel() {


    var alertCreateResponse = mutableStateOf<CreateAlertResponseModel?>(null)
    var alertListResponse = mutableStateOf<ListAlertResponseModel?>(null)
    var alertCancelResponse = mutableStateOf<CancelAlertResponseModel?>(null)

    var apiError = mutableStateOf<String?>(null)
    var isApiLoading = mutableStateOf(false)
    var isEmpty = mutableStateOf(false)


    fun alertCreate(
        frequency: String,
        price: String,
        type: String,
        walletAddress: String,
        tokenName: String,
        tokenSymbol: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            apiError.value = null
            isApiLoading.value = true
            try {
                val result = PayFundRetrofitInstance.payFundApi.alertCreate(
                    CreateAlertRequestModel(
                        walletAddress = walletAddress.lowercase(),
                        type = type,
                        price = price.replace(",",""),
                        token = Token(
                            name = tokenName,
                            symbol = tokenSymbol
                        ),
                        frequency = frequency
                    )
                )
                if (result.isSuccessful && result.body() != null) {
                    val response = result.body()!!

                    if (response.isSuccess) {
                        alertCreateResponse.value = response

                    } else {
                        apiError.value = response.msg
                    }
                } else {
                    val gson = Gson()
                    val error: ErrorResponseModel = gson.fromJson(result.errorBody()?.charStream(), ErrorResponseModel::class.java)
                    apiError.value = error.message
                }
            } catch (e: Exception) {
                apiError.value = e.message
            } finally {
                isApiLoading.value = false
            }
        }
    }

    fun alertCancel(
        alerts: List<String>,
        walletAddress: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            apiError.value = null
            isApiLoading.value = true
            try {
                val result = PayFundRetrofitInstance.payFundApi.alertCancel(
                    CancelAlertRequestModel(
                        walletAddress = walletAddress.lowercase(),
                        alerts = alerts
                    )
                )
                if (result.isSuccessful && result.body() != null) {
                    val response = result.body()!!
                    if (response.isSuccess) {
                        alertCancelResponse.value = response
                    } else {
                        apiError.value = response.msg
                    }
                } else {
                    apiError.value = getError(result)?.message
                }
            } catch (e: Exception) {
                apiError.value = e.message
            } finally {
                isApiLoading.value = false
            }
        }
    }

    fun alertList(walletAddress: String) {
        viewModelScope.launch(Dispatchers.IO) {
            alertListResponse.value = null
            isEmpty.value = false
            apiError.value = null
            isApiLoading.value = true
            try {
                val result = PayFundRetrofitInstance.payFundApi.alertList(
                    page = "1",
                    limit = "50",
                    ListAlertRequestModel(
                        walletAddress = walletAddress.lowercase()
                    )
                )
                if (result.isSuccessful && result.body() != null) {
                    val response = result.body()!!
                    if (response.isSuccess) {
                        alertListResponse.value = response
                        if (response.data.alerts.isEmpty()) {
                            isEmpty.value = true
                        }
                    } else {
                        apiError.value = response.msg
                    }
                } else {
                    apiError.value = getError(result)?.message
                }
            } catch (e: Exception) {
                apiError.value = e.message
            } finally {
                isApiLoading.value = false
            }
        }
    }

    fun refreshData(walletAddress: String) {
        alertListResponse.value = null
        isEmpty.value = false
        apiError.value = null
        alertList(walletAddress)
    }
}

