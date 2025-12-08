package com.payfunds.wallet.modules.syncerror

import com.payfunds.wallet.core.IAdapterManager
import com.payfunds.wallet.core.managers.BtcBlockchainManager
import com.payfunds.wallet.core.managers.EvmBlockchainManager
import com.payfunds.wallet.entities.Wallet

class SyncErrorService(
    private val wallet: Wallet,
    private val adapterManager: IAdapterManager,
    val reportEmail: String,
    private val btcBlockchainManager: BtcBlockchainManager,
    private val evmBlockchainManager: EvmBlockchainManager
) {

    val blockchainWrapper by lazy {
        btcBlockchainManager.blockchain(wallet.token.blockchainType)?.let {
            SyncErrorModule.BlockchainWrapper(it, SyncErrorModule.BlockchainWrapper.Type.Bitcoin)
        } ?: run {
            evmBlockchainManager.getBlockchain(wallet.token)?.let {
                SyncErrorModule.BlockchainWrapper(it, SyncErrorModule.BlockchainWrapper.Type.Evm)
            }
        }
    }

    val coinName: String = wallet.coin.name

    val sourceChangeable = blockchainWrapper != null

    fun retry() {
        adapterManager.refreshByWallet(wallet)
    }
}
