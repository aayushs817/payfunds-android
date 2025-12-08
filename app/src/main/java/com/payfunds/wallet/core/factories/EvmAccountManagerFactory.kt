package com.payfunds.wallet.core.factories

import io.horizontalsystems.marketkit.models.BlockchainType
import com.payfunds.wallet.core.IAccountManager
import com.payfunds.wallet.core.IWalletManager
import com.payfunds.wallet.core.managers.EvmAccountManager
import com.payfunds.wallet.core.managers.EvmKitManager
import com.payfunds.wallet.core.managers.MarketKitWrapper
import com.payfunds.wallet.core.managers.TokenAutoEnableManager

class EvmAccountManagerFactory(
    private val accountManager: IAccountManager,
    private val walletManager: IWalletManager,
    private val marketKit: MarketKitWrapper,
    private val tokenAutoEnableManager: TokenAutoEnableManager
) {

    fun evmAccountManager(blockchainType: BlockchainType, evmKitManager: EvmKitManager) =
        EvmAccountManager(
            blockchainType,
            accountManager,
            walletManager,
            marketKit,
            evmKitManager,
            tokenAutoEnableManager
        )

}
