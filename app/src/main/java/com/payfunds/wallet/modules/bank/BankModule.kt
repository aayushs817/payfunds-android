package com.payfunds.wallet.modules.bank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object BankModule {
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BankViewModel() as T
        }
    }
}
