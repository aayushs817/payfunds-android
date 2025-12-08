package com.payfunds.wallet.core.managers

import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.marketkit.models.TokenQuery
import com.payfunds.wallet.core.ICoinManager
import com.payfunds.wallet.core.IWalletManager

class CoinManager(
    private val marketKit: MarketKitWrapper,
    private val walletManager: IWalletManager
) : ICoinManager {

    override fun getToken(query: TokenQuery): Token? {
        return marketKit.token(query) ?: customToken(query)
    }

    private fun customToken(tokenQuery: TokenQuery): Token? {
        return walletManager.activeWallets.find { it.token.tokenQuery == tokenQuery }?.token
    }
}
