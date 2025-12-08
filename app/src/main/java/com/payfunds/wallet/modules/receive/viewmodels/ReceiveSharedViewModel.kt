package com.payfunds.wallet.modules.receive.viewmodels

import androidx.lifecycle.ViewModel
import io.horizontalsystems.marketkit.models.FullCoin
import com.payfunds.wallet.core.App
import com.payfunds.wallet.entities.Account
import com.payfunds.wallet.entities.Wallet
import com.payfunds.wallet.modules.receive.ui.UsedAddressesParams

class ReceiveSharedViewModel : ViewModel() {

    var wallet: Wallet? = null
    var coinUid: String? = null
    var usedAddressesParams: UsedAddressesParams? = null

    val activeAccount: Account?
        get() = App.accountManager.activeAccount

    fun fullCoin(): FullCoin? {
        val coinUid = coinUid ?: return null
        return App.marketKit.fullCoins(listOf(coinUid)).firstOrNull()
    }

}