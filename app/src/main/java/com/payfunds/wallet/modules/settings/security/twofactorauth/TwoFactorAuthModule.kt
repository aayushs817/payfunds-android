package com.payfunds.wallet.modules.settings.security.twofactorauth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

object TwoFactorAuthModule {
    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TwoFactorAuthViewModel() as T
        }
    }
}