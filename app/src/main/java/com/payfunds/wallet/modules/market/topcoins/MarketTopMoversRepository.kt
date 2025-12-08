package com.payfunds.wallet.modules.market.topcoins

import io.horizontalsystems.marketkit.models.TopMovers
import com.payfunds.wallet.core.managers.MarketKitWrapper
import com.payfunds.wallet.entities.Currency
import io.reactivex.Single

class MarketTopMoversRepository(
    private val marketKit: MarketKitWrapper
) {

    fun getTopMovers(baseCurrency: Currency): Single<TopMovers> =
        marketKit.topMoversSingle(baseCurrency.code)

}
