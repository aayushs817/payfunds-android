package com.payfunds.wallet.modules.receive.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.marketkit.models.TokenType
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.IWalletManager
import com.payfunds.wallet.core.accountTypeDerivation
import com.payfunds.wallet.modules.receive.ui.AddressFormatItem

class DerivationSelectViewModel(coinUid: String, walletManager: IWalletManager) : ViewModel() {
    val items = walletManager.activeWallets
        .filter {
            it.coin.uid == coinUid
        }
        .mapNotNull { wallet ->
            val derivation =
                (wallet.token.type as? TokenType.Derived)?.derivation ?: return@mapNotNull null
            val accountTypeDerivation = derivation.accountTypeDerivation

            AddressFormatItem(
                title = accountTypeDerivation.addressType + accountTypeDerivation.recommended,
                subtitle = accountTypeDerivation.value.uppercase(),
                wallet = wallet
            )
        }

    class Factory(private val coinUid: String) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DerivationSelectViewModel(coinUid, App.walletManager) as T
        }
    }
}