package com.payfunds.wallet.modules.alert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object AlertModule {
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AlertViewModel() as T
        }
    }
}