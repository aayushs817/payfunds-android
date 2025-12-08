package com.payfunds.wallet.core.managers

import io.horizontalsystems.binancechainkit.BinanceChainKit
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.IBinanceKitManager
import com.payfunds.wallet.core.UnsupportedAccountException
import com.payfunds.wallet.entities.Account
import com.payfunds.wallet.entities.AccountType
import com.payfunds.wallet.entities.Wallet

class BinanceKitManager : IBinanceKitManager {
    private var kit: BinanceChainKit? = null
    private var useCount = 0
    private var currentAccount: Account? = null

    override val binanceKit: BinanceChainKit?
        get() = kit

    override val statusInfo: Map<String, Any>?
        get() = kit?.statusInfo()

    override fun binanceKit(wallet: Wallet): BinanceChainKit {
        val account = wallet.account
        val accountType = account.type

        if (kit != null && currentAccount != account) {
            kit?.stop()
            kit = null
            currentAccount = null
        }

        if (kit == null) {
            if (accountType !is AccountType.Mnemonic)
                throw UnsupportedAccountException()

            useCount = 0

            kit = createKitInstance(accountType, account)
            currentAccount = account
        }

        useCount++
        return kit!!
    }

    private fun createKitInstance(
        accountType: AccountType.Mnemonic,
        account: Account
    ): BinanceChainKit {
        val networkType = BinanceChainKit.NetworkType.MainNet

        val kit = BinanceChainKit.instance(
            App.instance,
            accountType.words,
            accountType.passphrase,
            account.id,
            networkType
        )
        kit.refresh()

        return kit
    }

    override fun unlink(account: Account) {
        if (currentAccount != account) return

        useCount -= 1

        if (useCount < 1) {
            kit?.stop()
            kit = null
            currentAccount = null
        }
    }

}
