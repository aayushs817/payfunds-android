package com.payfunds.wallet.modules.bank

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CardControlModule {
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CardControlViewModel() as T
        }
    }
}
