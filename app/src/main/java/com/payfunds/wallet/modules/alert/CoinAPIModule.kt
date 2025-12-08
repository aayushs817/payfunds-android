package com.payfunds.wallet.modules.alert

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object CoinAPIModule {
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CoinAPIViewModel() as T
        }
    }
}
