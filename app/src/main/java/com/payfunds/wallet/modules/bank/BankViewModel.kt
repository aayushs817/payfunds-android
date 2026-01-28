package com.payfunds.wallet.modules.bank

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.payfunds.wallet.core.App
import com.payfunds.wallet.entities.ViewState
import com.payfunds.wallet.modules.settings.security.twofactorauth.CrateUserTokenManager
import com.payfunds.wallet.network.PayFundRetrofitInstance
import com.payfunds.wallet.network.request_model.request_card.RequestCardRequestModal
import com.payfunds.wallet.network.request_model.withdraw.WithdrawRequestModal
import com.payfunds.wallet.network.response_model.get_card_transactions.Transaction
import com.payfunds.wallet.network.response_model.get_user_details.GetUserDetailsResponseModal
import com.payfunds.wallet.network.response_model.card_balance.GetAccountBalanceResponseModal
import com.payfunds.wallet.network.response_model.deposit_info.DepositInfoResponseModal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BankViewModel : ViewModel() {

    var uiState by mutableStateOf<ViewState>(ViewState.Loading)
        private set

    var userDetails by mutableStateOf<GetUserDetailsResponseModal?>(null)
        private set

    val transactions = mutableStateListOf<Transaction>()

    var cardDetails by mutableStateOf<com.payfunds.wallet.network.response_model.get_card_info.Card?>(null)
        private set

    var cardBalance by mutableStateOf<GetAccountBalanceResponseModal?>(null)
        private set

    private val tokenManager = CrateUserTokenManager(App.instance)

    init {
        fetchUserDetails()
        fetchTransactions()
        fetchAccountBalance()
    }

    fun fetchUserDetails() {
        viewModelScope.launch {
            if (userDetails == null) {
                uiState = ViewState.Loading
            }
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = "Bearer " + tokenManager.crateUserGetToken()
                    PayFundRetrofitInstance.holoBankApi.getUserDetails(token)
                }

                if (response.isSuccessful) {
                    val body = response.body()!!
                    userDetails = body
                    uiState = ViewState.Success
                    fetchTransactions()
                    if (body.data.bankDetails.cards.isNotEmpty() || body.data.bankDetails.cardRequests) {
                        fetchCardHtml()
                        fetchAccountBalance()
                    }
                } else if (response.code() == 404) {
                    uiState = ViewState.Error(Exception("UserNotPresent"))
                } else {
                    uiState = ViewState.Error(Exception(response.message()))
                }
            } catch (e: Exception) {
                uiState = ViewState.Error(e)
                Log.e("BankViewModel", "fetchUserDetails: ", e)
            }
        }
    }

    fun fetchTransactions() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = "Bearer " + tokenManager.crateUserGetToken()
                    PayFundRetrofitInstance.holoBankApi.getTransaction(token, 10, 0)
                }

                if (response.isSuccessful && response.body() != null) {
                    transactions.clear()
                    transactions.addAll(response.body()!!.data.transactions)
                }
            } catch (e: Exception) {
                Log.e("BankViewModel", "fetchTransactions: ", e)
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

                if (response.isSuccessful && response.body() != null) {
                    cardDetails = response.body()?.data?.card
                }
            } catch (e: Exception) {
                Log.e("BankViewModel", "fetchCardHtml: ", e)
            }
        }
    }

    fun fetchAccountBalance() {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = "Bearer " + tokenManager.crateUserGetToken()
                    PayFundRetrofitInstance.holoBankApi.getAccountBalance(token)
                }

                if (response.isSuccessful && response.body() != null) {
                    cardBalance = response.body()
                }
            } catch (e: Exception) {
                Log.e("BankViewModel", "fetchCardBalance: ", e)
            }
        }
    }

    fun requestCard() {
        viewModelScope.launch {
            uiState = ViewState.Loading
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = "Bearer " + tokenManager.crateUserGetToken()
                    PayFundRetrofitInstance.holoBankApi.requestCard(token, RequestCardRequestModal(type = "VIRTUAL_VISA_REAP"))
                }

                if (response.isSuccessful) {
                    fetchUserDetails()
                } else {
                    uiState = ViewState.Error(Exception(response.body()?.message ?: response.message()))
                }
            } catch (e: Exception) {
                uiState = ViewState.Error(e)
                Log.e("BankViewModel", "requestCard: ", e)
            }
        }
    }

    fun withdraw(amount: String, walletAddress: String, onComplete: (String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = "Bearer " + tokenManager.crateUserGetToken()
                    PayFundRetrofitInstance.holoBankApi.withdraw(token, WithdrawRequestModal(amount, walletAddress))
                }

                if (response.isSuccessful) {
                    fetchUserDetails()
                    onComplete(null)
                } else {
                    onComplete(response.message())
                }
            } catch (e: Exception) {
                onComplete(e.message ?: "An error occurred")
            }
        }
    }

    fun fetchDepositInfo(onComplete: (String?, String?) -> Unit) {
        viewModelScope.launch {
            try {
                val response = withContext(Dispatchers.IO) {
                    val token = "Bearer " + tokenManager.crateUserGetToken()
                    PayFundRetrofitInstance.holoBankApi.depositInfo(token, "REAP")
                }

                if (response.isSuccessful && response.body() != null) {
                    onComplete(response.body()!!.data.address, null)
                } else {
                    onComplete(null, response.message())
                }
            } catch (e: Exception) {
                onComplete(null, e.message ?: "An error occurred")
            }
        }
    }
}
