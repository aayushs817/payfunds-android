package com.payfunds.wallet.core.factories

import io.horizontalsystems.marketkit.models.BlockchainType
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.IFeeRateProvider
import com.payfunds.wallet.core.providers.BitcoinCashFeeRateProvider
import com.payfunds.wallet.core.providers.BitcoinFeeRateProvider
import com.payfunds.wallet.core.providers.DashFeeRateProvider
import com.payfunds.wallet.core.providers.ECashFeeRateProvider
import com.payfunds.wallet.core.providers.LitecoinFeeRateProvider

object FeeRateProviderFactory {
    fun provider(blockchainType: BlockchainType): IFeeRateProvider? {
        val feeRateProvider = App.feeRateProvider

        return when (blockchainType) {
            is BlockchainType.Bitcoin -> BitcoinFeeRateProvider(feeRateProvider)
            is BlockchainType.Litecoin -> LitecoinFeeRateProvider(feeRateProvider)
            is BlockchainType.BitcoinCash -> BitcoinCashFeeRateProvider(feeRateProvider)
            is BlockchainType.ECash -> ECashFeeRateProvider()
            is BlockchainType.Dash -> DashFeeRateProvider(feeRateProvider)
            else -> null
        }
    }

}
