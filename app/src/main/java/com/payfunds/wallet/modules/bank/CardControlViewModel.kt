package com.payfunds.wallet.modules.bank

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.payfunds.wallet.core.App
import com.payfunds.wallet.entities.ViewState
import com.payfunds.wallet.modules.settings.security.twofactorauth.CrateUserTokenManager
import com.payfunds.wallet.network.PayFundRetrofitInstance
import com.payfunds.wallet.network.request_model.card_freeze.CardFreezeRequestModal
import com.payfunds.wallet.network.request_model.update_card_3ds.UpdateCard3dsForwardingRequestModal
import com.payfunds.wallet.network.request_model.update_card_label.UpdateCardLabelRequestModal
import com.payfunds.wallet.network.request_model.update_card_pin.UpdateCardPinRequestModal
import com.google.gson.Gson
import com.payfunds.wallet.modules.coin.overview.HudMessage
import com.payfunds.wallet.network.response_model.get_card_details.GetCardDetailsResponseModal
import com.payfunds.wallet.network.response_model.holobank.HolobankErrorResponse
import android.util.Log
import com.payfunds.wallet.network.response_model.get_card_info.Card
import com.payfunds.wallet.network.response_model.get_card_info.Data
import com.payfunds.wallet.network.response_model.get_card_info.GetCardInfoResponseModal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class CardControlViewModel : ViewModel() {

    var uiState by mutableStateOf<ViewState>(ViewState.Loading)
        private set

    var cardDetails by mutableStateOf<GetCardDetailsResponseModal?>(null)
        private set

    var isFrozen by mutableStateOf(false)
        private set

    var selected3DSMethod by mutableStateOf("")
        private set

    var cardSensitiveDetails by mutableStateOf<GetCardInfoResponseModal?>(null)
        private set

    private val tokenManager = CrateUserTokenManager(App.instance)

    init {
        fetchCardDetails()
        fetch3dsForwarding()
    }

    fun fetchCardDetails() {
        viewModelScope.launch {
            uiState = ViewState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = "Bearer " + tokenManager.crateUserGetToken()
                    PayFundRetrofitInstance.holoBankApi.getCardDetails(token)
                }

                if (response.isSuccessful) {
                    uiState = ViewState.Success
                    fetchCardHtml()
                } else {
                    uiState = ViewState.Error(Exception(getErrorMessage(response)))
                }
            } catch (e: Exception) {
                uiState = ViewState.Error(e)
            }
        }
    }

    fun updatePin(pin: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            uiState = ViewState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = "Bearer " + tokenManager.crateUserGetToken()
                    PayFundRetrofitInstance.holoBankApi.updateCardPin(
                        token,
                        UpdateCardPinRequestModal(pin)
                    )
                }

                if (response.isSuccessful) {
                    uiState = ViewState.Success
                    onSuccess(response.body()?.message ?: "PIN updated successfully")
                } else {
                    uiState = ViewState.Error(Exception(getErrorMessage(response)))
                }
            } catch (e: Exception) {
                uiState = ViewState.Error(e)
            }
        }
    }

    fun toggleFreeze(freeze: Boolean) {
        viewModelScope.launch {
            uiState = ViewState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = "Bearer " + tokenManager.crateUserGetToken()
                    PayFundRetrofitInstance.holoBankApi.cardFreeze(
                        token,
                        CardFreezeRequestModal(freeze)
                    )
                }

                if (response.isSuccessful) {
                    isFrozen = freeze
                } else {
                    uiState = ViewState.Error(Exception(getErrorMessage(response)))
                }
            } catch (e: Exception) {
                uiState = ViewState.Error(e)
            }
        }
    }

    fun fetch3dsForwarding() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = "Bearer " + tokenManager.crateUserGetToken()
                    PayFundRetrofitInstance.holoBankApi.getCard3dsForwarding(token)
                }

                if (response.isSuccessful) {
                    selected3DSMethod =  response.body()?.data?.forwardingMethod ?: ""
                }
            } catch (e: Exception) {
                uiState = ViewState.Error(e)
            }
        }
    }

    fun fetchCardHtml() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = "Bearer " + tokenManager.crateUserGetToken()
                    PayFundRetrofitInstance.holoBankApi.getCardHtml(token)
                }

                if (response.isSuccessful) {
                    cardSensitiveDetails = response.body()
                }
            } catch (e: Exception) {
                Log.e("CardControlViewModel", "fetchCardHtml: ", e)
            }
        }
    }

    fun update3dsForwarding(method: String, status: String) {
        viewModelScope.launch {
            uiState = ViewState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = "Bearer " + tokenManager.crateUserGetToken()
                    PayFundRetrofitInstance.holoBankApi.updateCard3dsForwarding(
                        token,
                        UpdateCard3dsForwardingRequestModal(method, status)
                    )
                }

                if (response.isSuccessful) {
                    selected3DSMethod = method
                    uiState = ViewState.Success
                } else {
                    uiState = ViewState.Error(Exception(getErrorMessage(response)))
                }
            } catch (e: Exception) {
                uiState = ViewState.Error(e)
            }
        }
    }

    fun deleteCard(onSuccess: () -> Unit) {
        viewModelScope.launch {
            uiState = ViewState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = "Bearer " + tokenManager.crateUserGetToken()
                    PayFundRetrofitInstance.holoBankApi.deleteCard(token)
                }

                if (response.isSuccessful) {
                    uiState = ViewState.Success
                    onSuccess()
                } else {
                    uiState = ViewState.Error(Exception(getErrorMessage(response)))
                }
            } catch (e: Exception) {
                uiState = ViewState.Error(e)
            }
        }
    }

    fun updateCardLabel(label: String, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            uiState = ViewState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = "Bearer " + tokenManager.crateUserGetToken()
                    PayFundRetrofitInstance.holoBankApi.updateCardLabel(
                        token,
                        UpdateCardLabelRequestModal(label)
                    )
                }

                if (response.isSuccessful) {
                    uiState = ViewState.Success
                    onSuccess(response.body()?.message ?: "Label updated successfully")
                } else {
                    uiState = ViewState.Error(Exception(getErrorMessage(response)))
                }
            } catch (e: Exception) {
                uiState = ViewState.Error(e)
            }
        }
    }

    private fun getErrorMessage(response: Response<*>): String {
        return try {
            val errorBody = response.errorBody()?.string()
            val errorResponse = Gson().fromJson(errorBody, HolobankErrorResponse::class.java)
            errorResponse?.message ?: response.message()
        } catch (e: Exception) {
            response.message()
        }
    }
}
