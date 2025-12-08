package com.payfunds.wallet.modules.settings.security.twofactorauth

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.DashboardObject
import com.payfunds.wallet.network.PayFundRetrofitInstance
import com.payfunds.wallet.network.request_model.two_factor_auth.disable.TwoFADisableRequestModel
import com.payfunds.wallet.network.request_model.two_factor_auth.enable.TwoFAEnableRequestModel
import com.payfunds.wallet.network.request_model.two_factor_auth.recover.TwoFARecoverRequestModel
import com.payfunds.wallet.network.request_model.two_factor_auth.verify.TwoFAVerifyRequestModel
import com.payfunds.wallet.network.response_model.two_factor_auth.disable.TwoFADisableResponseModel
import com.payfunds.wallet.network.response_model.two_factor_auth.enable.TwoFAEnableResponseModel
import com.payfunds.wallet.network.response_model.two_factor_auth.error.TwoFAErrorResponseModel
import com.payfunds.wallet.network.response_model.two_factor_auth.qr.TwoFAQRCodeResponseModel
import com.payfunds.wallet.network.response_model.two_factor_auth.recover.TwoFARecoverResponseModel
import com.payfunds.wallet.network.response_model.two_factor_auth.verify.TwoFAVerifyResponseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class TwoFactorAuthViewModel : ViewModel() {

    var twoFADisableResponse = mutableStateOf<TwoFADisableResponseModel?>(null)
    var twoFAEnableResponse = mutableStateOf<TwoFAEnableResponseModel?>(null)
    var twoFAGenerateQRResponse = mutableStateOf<TwoFAQRCodeResponseModel?>(null)
    var twoFAVerifyResponse = mutableStateOf<TwoFAVerifyResponseModel?>(null)
    var twoFARecoverResponse = mutableStateOf<TwoFARecoverResponseModel?>(null)

    var isError = mutableStateOf<String?>(null)
    var isLoading = mutableStateOf(false)

    private val tokenManager = CrateUserTokenManager(App.instance)

    fun twoFAGenerateQRCode() {
        viewModelScope.launch(Dispatchers.IO) {
            isError.value = null
            isLoading.value = true
            try {
                val result = PayFundRetrofitInstance.payFundApi.twoFactorAuthGenerateQR(getToken())
                if (result.isSuccessful && result.body() != null) {
                    val response = result.body()!!
                    if (response.isSuccess) {
                        twoFAGenerateQRResponse.value = response
                    } else {
                        isError.value = response.message
                    }
                } else {
                    isError.value = getError(result)?.message
                }
            } catch (e: Exception) {
                isError.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun twoFADisable(
        otp: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            isError.value = null
            isLoading.value = true
            try {
                val result = PayFundRetrofitInstance.payFundApi.twoFactorAuthDisable(
                    token = getToken(),
                    twoFADisableRequestModel = TwoFADisableRequestModel(otp)
                )
                if (result.isSuccessful && result.body() != null) {
                    val response = result.body()!!
                    if (response.isSuccess) {
                        twoFADisableResponse.value = response
                        DashboardObject.updateIsDashboardOpened(true)
                    } else {
                        isError.value = response.message
                    }
                } else {
                    isError.value = getError(result)?.message
                }
            } catch (e: Exception) {
                isError.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun twoFAEnable(otp: String) {
        viewModelScope.launch(Dispatchers.IO) {
            isError.value = null
            isLoading.value = true
            try {
                val result = PayFundRetrofitInstance.payFundApi.twoFactorAuthEnable(
                    token = getToken(),
                    twoFAEnableRequestModel = TwoFAEnableRequestModel(otp = otp)
                )
                if (result.isSuccessful && result.body() != null) {
                    val response = result.body()!!
                    if (response.isSuccess) {
                        twoFAEnableResponse.value = response
                    } else {
                        isError.value = response.message
                    }
                } else {
                    isError.value = getError(result)?.message
                }
            } catch (e: Exception) {
                isError.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun twoFAVerify(walletAddress: String, otp: String) {
        viewModelScope.launch(Dispatchers.IO) {
            isError.value = null
            isLoading.value = true
            try {
                val result = PayFundRetrofitInstance.payFundApi.twoFactorAuthVerify(
                    TwoFAVerifyRequestModel(walletAddress = walletAddress.lowercase(), otp = otp)
                )
                if (result.isSuccessful && result.body() != null) {
                    val response = result.body()!!
                    if (response.isSuccess) {
                        twoFAVerifyResponse.value = response
                        if (response.data.token.isNotEmpty()) {
                            tokenManager.createUserSaveToken(result.body()!!.data.token)
                        }
                    } else {
                        isError.value = response.message
                    }
                } else {
                    isError.value = getError(result)?.message
                }
            } catch (e: Exception) {
                isError.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    fun twoFARecovery(walletAddress: String, recoveryCode: String) {
        viewModelScope.launch(Dispatchers.IO) {
            isError.value = null
            isLoading.value = true
            try {
                val result = PayFundRetrofitInstance.payFundApi.twoFactorAuthRecover(
                    TwoFARecoverRequestModel(
                        walletAddress = walletAddress.lowercase(),
                        recoveryCode = recoveryCode
                    )
                )
                if (result.isSuccessful && result.body() != null) {
                    val response = result.body()!!
                    if (response.isSuccess) {
                        twoFARecoverResponse.value = response
                    } else {
                        isError.value = response.message
                    }
                } else {
                    isError.value = getError(result)?.message
                }
            } catch (e: Exception) {
                isError.value = e.message
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun getError(result: Response<*>): TwoFAErrorResponseModel? {
        val gson = Gson()
        return result.errorBody()?.charStream()?.let {
            gson.fromJson(it, TwoFAErrorResponseModel::class.java)
        }
    }

    private fun getToken(): String {
        return "Bearer " + tokenManager.crateUserGetToken()
    }
}
