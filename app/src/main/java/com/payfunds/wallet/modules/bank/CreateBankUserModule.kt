package com.payfunds.wallet.modules.bank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object CreateBankUserModule {
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreateBankUserViewModel() as T
        }
    }
}
