package com.payfunds.wallet.modules.bank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object BankTransactionsModule {
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BankTransactionsViewModel() as T
        }
    }
}
