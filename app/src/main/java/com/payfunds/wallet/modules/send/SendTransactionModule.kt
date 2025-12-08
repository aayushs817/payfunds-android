package com.payfunds.wallet.modules.send

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object SendTransactionModule {
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SendTransactionViewModel() as T
        }
    }
}