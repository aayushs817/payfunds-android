package com.payfunds.wallet.modules.send

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.DashboardObject
import com.payfunds.wallet.modules.settings.security.twofactorauth.CrateUserTokenManager
import com.payfunds.wallet.network.PayFundRetrofitInstance
import com.payfunds.wallet.network.request_model.send_transaction_details.SendTransactionDetailsRequestModel
import com.payfunds.wallet.network.response_model.send_transaction_details.SendTransactionDetailsResponseModel
import com.payfunds.wallet.network.response_model.two_factor_auth.disable.TwoFADisableResponseModel
import com.payfunds.wallet.network.response_model.two_factor_auth.error.TwoFAErrorResponseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Response

class SendTransactionViewModel : ViewModel() {

    var sendTransactionDetailsResponseModel = mutableStateOf<SendTransactionDetailsResponseModel?>(null)

    var isError = mutableStateOf<String?>(null)
    var isLoading = mutableStateOf(false)

    private val tokenManager = CrateUserTokenManager(App.instance)

    fun sendTransactionDetails(
        contractAddress: String?,
        fromAddress: String,
        symbol: String,
        toAddress: String,
        totalAmount: String,
        txnHash: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {

            isError.value = null
            isLoading.value = true
            try {
                val result = PayFundRetrofitInstance.payFundApi.sendTransactionDetails(
                    token = getToken(),
                    sendTransactionDetailsRequestModel = SendTransactionDetailsRequestModel(
                        contractAddress = contractAddress,
                        fromAddress = fromAddress,
                        symbol = symbol,
                        toAddress = toAddress,
                        totalAmount = totalAmount.toDouble(),
                        txnHash = txnHash
                    )
                )
                if (result.isSuccessful && result.body() != null) {
                    val response = result.body()!!

                    sendTransactionDetailsResponseModel.value = response

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