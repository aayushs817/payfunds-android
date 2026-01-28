package com.payfunds.wallet.modules.bank

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.payfunds.wallet.core.App
import com.payfunds.wallet.entities.ViewState
import com.payfunds.wallet.modules.settings.security.twofactorauth.CrateUserTokenManager
import com.payfunds.wallet.network.PayFundRetrofitInstance
import com.payfunds.wallet.network.response_model.get_card_transactions.Transaction
import com.payfunds.wallet.network.response_model.holobank.HolobankErrorResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response

class BankTransactionsViewModel : ViewModel() {

    var uiState by mutableStateOf<ViewState>(ViewState.Success)
        private set

    val transactions = mutableStateListOf<Transaction>()

    private var currentPage = 1
    private var hasNextPage = true
    private var isLoadingMore = false

    private val limit = 20
    private val tokenManager = CrateUserTokenManager(App.instance)

    init {
        fetchTransactions()
    }

    fun fetchTransactions(refresh: Boolean = false) {
        if (refresh) {
            currentPage = 1
            hasNextPage = true
            transactions.clear()
        }

        if (!hasNextPage || (uiState is ViewState.Loading && !refresh) || isLoadingMore) return

        viewModelScope.launch {
            if (refresh || transactions.isEmpty()) {
                uiState = ViewState.Loading
            } else {
                isLoadingMore = true
            }

            try {
                val response = withContext(Dispatchers.IO) {
                    val token = "Bearer " + tokenManager.crateUserGetToken()
                    val offset = (currentPage - 1) * limit
                    PayFundRetrofitInstance.holoBankApi.getTransaction(token, limit, offset)
                }

                if (response.isSuccessful) {
                    val body = response.body()!!
                    transactions.addAll(body.data.transactions)
                    hasNextPage = body.data.pagination.hasNextPage
                    currentPage++
                    uiState = ViewState.Success
                } else {
                    uiState = ViewState.Error(Exception(getErrorMessage(response)))
                }
            } catch (e: Exception) {
                uiState = ViewState.Error(e)
            } finally {
                isLoadingMore = false
            }
        }
    }

    fun loadNextPage() {
        if (!isLoadingMore && hasNextPage) {
            fetchTransactions()
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
