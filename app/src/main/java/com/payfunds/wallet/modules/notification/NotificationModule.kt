package com.payfunds.wallet.modules.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object NotificationModule {
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return NotificationViewModel() as T
        }
    }
}