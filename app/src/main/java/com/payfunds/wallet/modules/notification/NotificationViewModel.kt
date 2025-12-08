package com.payfunds.wallet.modules.notification

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.payfunds.wallet.core.App
import com.payfunds.wallet.modules.settings.security.twofactorauth.CrateUserTokenManager
import com.payfunds.wallet.network.PayFundRetrofitInstance
import com.payfunds.wallet.network.request_model.fcm.FCMRegisterRequestModel
import com.payfunds.wallet.network.request_model.notification_mark_as_read.NotificationMarkAsReadRequestModel
import com.payfunds.wallet.network.response_model.fcm.FCMRegisterResponseModel
import com.payfunds.wallet.network.response_model.get_all_notification.GetAllNotificationResponseModel
import com.payfunds.wallet.network.response_model.notification_mark_as_read.NotificationMarkAsReadResponseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {

    var fcmRegisterResponseModel = mutableStateOf<FCMRegisterResponseModel?>(null)
    var getAllNotificationResponseModel = mutableStateOf<GetAllNotificationResponseModel?>(null)
    var notificationMarkAsReadResponseModel =
        mutableStateOf<NotificationMarkAsReadResponseModel?>(null)
    var apiError = mutableStateOf<String?>(null)
    var isApiLoading = mutableStateOf(false)

    private val tokenManager = CrateUserTokenManager(App.instance)

    fun setFCMRegister(
        deviceId: String,
        fcmToken: String,
        os: String,
        walletAddress: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            apiError.value = null
            isApiLoading.value = true
            try {
                val result = PayFundRetrofitInstance.payFundApi.fcmRegister(
                    FCMRegisterRequestModel(
                        walletAddress = walletAddress.lowercase(),
                        deviceId = deviceId,
                        fcmToken = fcmToken,
                        os = os
                    )
                )
                if (result.isSuccessful && result.body() != null) {
                    val responseBody = result.body()!!
                    fcmRegisterResponseModel.value = responseBody
                } else {
                    val responseBody = result.body()!!
                    apiError.value = responseBody.msg
                }
                isApiLoading.value = false
            } catch (e: Exception) {
                apiError.value = e.message
                isApiLoading.value = false
                e.printStackTrace()
            }
        }
    }

    fun getAllNotification(
        startIndex: Int,
        itemsPerPage: Int,
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            apiError.value = null
            isApiLoading.value = true
            try {
                val result = PayFundRetrofitInstance.payFundApi.getAllNotifications(
                    token = getToken(),
                    startIndex = startIndex,
                    itemsPerPage = itemsPerPage
                )
                if (result.isSuccessful && result.body() != null) {
                    val responseBody = result.body()!!
                    getAllNotificationResponseModel.value = responseBody
                } else {
                    val responseBody = result.body()!!
                    apiError.value = responseBody.message
                }
                isApiLoading.value = false
            } catch (e: Exception) {
                apiError.value = e.message
                isApiLoading.value = false
                e.printStackTrace()
            }
        }
    }

    fun markAsReadNotification(
        notificationId: String? = null,
        isMarkAsRead: Boolean? = null
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            apiError.value = null
            isApiLoading.value = true
            try {
                val result = PayFundRetrofitInstance.payFundApi.notificationMarkAsRead(
                    token = getToken(),
                    NotificationMarkAsReadRequestModel(
                        _notification = notificationId,
                        isMarkAsAllRead = isMarkAsRead
                    )
                )
                if (result.isSuccessful && result.body() != null) {
                    val responseBody = result.body()!!
                    notificationMarkAsReadResponseModel.value = responseBody
                } else {
                    val responseBody = result.body()!!
                    apiError.value = responseBody.message
                }
                isApiLoading.value = false
            } catch (e: Exception) {
                apiError.value = e.message
                isApiLoading.value = false
                e.printStackTrace()
            }
        }
    }

    fun getToken(): String {
        return "Bearer " + tokenManager.crateUserGetToken()
    }
}