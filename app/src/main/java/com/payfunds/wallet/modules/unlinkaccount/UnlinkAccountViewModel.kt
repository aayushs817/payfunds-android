package com.payfunds.wallet.modules.unlinkaccount

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.payfunds.wallet.R
import com.payfunds.wallet.core.DashboardObject
import com.payfunds.wallet.core.IAccountManager
import com.payfunds.wallet.core.getWalletAddress
import com.payfunds.wallet.entities.Account
import com.payfunds.wallet.network.PayFundRetrofitInstance
import com.payfunds.wallet.network.request_model.remove_fcm.RemoveFcmRequestModel
import com.payfunds.wallet.ui.compose.TranslatableString
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UnlinkAccountViewModel(
    private val account: Account,
    private val accountManager: IAccountManager
) : ViewModel() {
    val accountName = account.name

    var confirmations by mutableStateOf<List<ConfirmationItem>>(listOf())
        private set
    var unlinkEnabled by mutableStateOf(false)
        private set
    var showDeleteWarning by mutableStateOf(false)
        private set

    val deleteButtonText = when {
        account.isWatchAccount -> R.string.ManageKeys_StopWatching
        else -> R.string.ManageKeys_Delete_FromPhone
    }

    init {
        if (account.isWatchAccount) {
            showDeleteWarning = true
        } else {
            confirmations = listOf(
                ConfirmationItem(ConfirmationType.ConfirmationRemove),
                ConfirmationItem(ConfirmationType.ConfirmationLos),
            )
        }

        updateUnlinkEnabledState()
    }

    fun toggleConfirm(item: ConfirmationItem) {
        val index = confirmations.indexOf(item)
        if (index != -1) {
            confirmations = confirmations.toMutableList().apply {
                this[index] = item.copy(confirmed = !item.confirmed)
            }

            updateUnlinkEnabledState()
        }
    }


    fun onUnlink() {
        removeFcmToken(account)
        accountManager.delete(account.id)
    }

    private fun removeFcmToken(account: Account) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = PayFundRetrofitInstance.payFundApi.removeFcmToken(
                    RemoveFcmRequestModel(
                        walletAddress = getWalletAddress(account)?.lowercase() ?: "",
                        deviceId = DashboardObject.deviceId,
                        os = "android"
                    )
                )
                if (result.isSuccessful && result.body() != null) {
                    val responseBody = result.body()!!
                } else {
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }


    private fun updateUnlinkEnabledState() {
        unlinkEnabled = confirmations.none { !it.confirmed }
    }
}

enum class ConfirmationType(val title: TranslatableString) {
    ConfirmationRemove(TranslatableString.ResString(R.string.ManageAccount_Delete_ConfirmationRemove)),
    ConfirmationLos(TranslatableString.ResString(R.string.ManageAccount_Delete_ConfirmationLose))
}

data class ConfirmationItem(val confirmationType: ConfirmationType, val confirmed: Boolean = false)