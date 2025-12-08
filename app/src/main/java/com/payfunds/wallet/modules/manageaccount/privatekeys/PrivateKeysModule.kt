package com.payfunds.wallet.modules.manageaccount.privatekeys

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.hdwalletkit.HDExtendedKey
import com.payfunds.wallet.core.App
import com.payfunds.wallet.entities.Account
import com.payfunds.wallet.modules.manageaccount.showextendedkey.ShowExtendedKeyModule

object PrivateKeysModule {

    class Factory(private val account: Account) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PrivateKeysViewModel(account, App.evmBlockchainManager) as T
        }
    }

    data class ViewState(
        val evmPrivateKey: String? = null,
        val bip32RootKey: ExtendedKey? = null,
        val accountExtendedPrivateKey: ExtendedKey? = null,
    )

    data class ExtendedKey(
        val hdKey: HDExtendedKey,
        val displayKeyType: ShowExtendedKeyModule.DisplayKeyType
    )
}