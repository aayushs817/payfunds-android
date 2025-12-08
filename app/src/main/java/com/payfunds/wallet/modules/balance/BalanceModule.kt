package com.payfunds.wallet.modules.balance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.marketkit.models.CoinPrice
import com.payfunds.wallet.R
import com.payfunds.wallet.core.AdapterState
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.BalanceData
import com.payfunds.wallet.core.Warning
import com.payfunds.wallet.entities.Wallet
import com.payfunds.wallet.modules.address.AddressHandlerFactory
import com.payfunds.wallet.modules.addtoken.AddTokenService
import com.payfunds.wallet.modules.balance.cex.BalanceCexRepositoryWrapper
import com.payfunds.wallet.modules.balance.cex.BalanceCexSorter
import com.payfunds.wallet.modules.balance.cex.BalanceCexViewModel
import com.payfunds.wallet.ui.compose.TranslatableString

object BalanceModule {
    class AccountsFactory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BalanceAccountsViewModel(App.accountManager) as T
        }
    }

    val tokenService = AddTokenService(
        App.coinManager,
        App.walletManager,
        App.accountManager,
        App.marketKit
    )

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val totalService = TotalService(
                App.currencyManager,
                App.marketKit,
                App.baseTokenManager,
                App.balanceHiddenManager
            )
            return BalanceViewModel(
                BalanceService.getInstance("wallet"),
                BalanceViewItemFactory(),
                App.balanceViewTypeManager,
                TotalBalance(totalService, App.balanceHiddenManager),
                App.localStorage,
                App.wcManager,
                AddressHandlerFactory(App.appConfigProvider.udnApiKey),
                App.priceManager,
                App.instance.isSwapEnabled,
                tokenService
            ) as T
        }
    }

    class FactoryCex : ViewModelProvider.Factory {

        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val totalService = TotalService(
                App.currencyManager,
                App.marketKit,
                App.baseTokenManager,
                App.balanceHiddenManager
            )

            return BalanceCexViewModel(
                TotalBalance(totalService, App.balanceHiddenManager),
                App.localStorage,
                App.balanceViewTypeManager,
                BalanceViewItemFactory(),
                BalanceCexRepositoryWrapper(App.cexAssetManager, App.connectivityManager),
                BalanceXRateRepository("wallet", App.currencyManager, App.marketKit),
                BalanceCexSorter(),
                App.cexProviderManager,
            ) as T
        }
    }

    data class BalanceItem(
        val wallet: Wallet,
        val balanceData: BalanceData,
        val state: AdapterState,
        val sendAllowed: Boolean,
        val coinPrice: CoinPrice?,
        val warning: BalanceWarning? = null
    ) {
        val fiatValue get() = coinPrice?.value?.let { balanceData.available.times(it) }
        val balanceFiatTotal get() = coinPrice?.value?.let { balanceData.total.times(it) }
    }

    sealed class BalanceWarning : Warning() {
        data object TronInactiveAccountWarning : BalanceWarning()
    }

    val BalanceWarning.warningText: WarningText
        get() = when (this) {
            BalanceWarning.TronInactiveAccountWarning -> WarningText(
                title = TranslatableString.ResString(R.string.Tron_TokenPage_AddressNotActive_Title),
                text = TranslatableString.ResString(R.string.Tron_TokenPage_AddressNotActive_Info),
            )
        }
}