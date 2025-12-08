package com.payfunds.wallet.core.managers

import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.marketkit.models.TokenQuery
import com.payfunds.wallet.core.IWalletManager
import com.payfunds.wallet.entities.Account
import com.payfunds.wallet.entities.Wallet

class WalletActivator(
    private val walletManager: IWalletManager,
    private val marketKit: MarketKitWrapper,
) {

    fun activateWallets(account: Account, tokenQueries: List<TokenQuery>) {
        val wallets = tokenQueries.mapNotNull { tokenQuery ->
            marketKit.token(tokenQuery)?.let { token ->
                Wallet(token, account)
            }
        }

        walletManager.save(wallets)
    }

    fun activateTokens(account: Account, tokens: List<Token>) {
        val wallets = mutableListOf<Wallet>()

        for (token in tokens) {
            wallets.add(Wallet(token, account))
        }

        walletManager.save(wallets)
    }

}
