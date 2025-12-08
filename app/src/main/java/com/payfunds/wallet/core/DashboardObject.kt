package com.payfunds.wallet.core

import android.util.Log
import com.payfunds.wallet.network.request_model.two_factor_auth.user.Wallet
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

object DashboardObject {
    var deviceId : String = "unknown"
    var notificationWalletAddress : String? = null
    var fcmToken : String? =null
    var isManageWalletOpened = false

    private val _isMultiFactor = MutableStateFlow<Boolean?>(null)
    val isMultiFactor = _isMultiFactor.asStateFlow()

    private val _is2FAEnabled = MutableStateFlow(false)
    val is2FAEnabled = _is2FAEnabled.asStateFlow()

    private val _isDashboardOpened = MutableStateFlow(false)
    val isDashboardOpened = _isDashboardOpened.asStateFlow()

    fun updateIsMultiFactor(newValue: Boolean?) {
        _isMultiFactor.value = newValue
    }

    fun update2FAEnabled(newValue: Boolean) {
        _is2FAEnabled.value = newValue
    }

    fun updateIsDashboardOpened(newValue: Boolean) {
        _isDashboardOpened.value = newValue
    }
}