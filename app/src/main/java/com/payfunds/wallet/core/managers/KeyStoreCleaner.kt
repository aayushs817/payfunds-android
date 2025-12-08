package com.payfunds.wallet.core.managers

import com.payfunds.wallet.core.IAccountManager
import com.payfunds.wallet.core.ILocalStorage
import com.payfunds.wallet.core.IWalletManager
import io.payfunds.core.IKeyStoreCleaner

class KeyStoreCleaner(
    private val localStorage: ILocalStorage,
    private val accountManager: IAccountManager,
    private val walletManager: IWalletManager
) : IKeyStoreCleaner {

    override var encryptedSampleText: String?
        get() = localStorage.encryptedSampleText
        set(value) {
            localStorage.encryptedSampleText = value
        }

    override fun cleanApp() {
        accountManager.clear()
        walletManager.clear()
        localStorage.clear()
    }
}
