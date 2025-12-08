package com.payfunds.wallet.modules.multiswap

import io.horizontalsystems.ethereumkit.models.RpcSource
import io.horizontalsystems.marketkit.models.BlockchainType
import com.payfunds.wallet.core.App

class EvmBlockchainHelper(private val blockchainType: BlockchainType) {
    val chain by lazy { App.evmBlockchainManager.getChain(blockchainType) }

    fun getRpcSourceHttp(): RpcSource.Http {
        val httpSyncSource = App.evmSyncSourceManager.getHttpSyncSource(blockchainType)
        return httpSyncSource?.rpcSource as? RpcSource.Http
            ?: throw IllegalStateException("No HTTP RPC Source for blockchain $blockchainType")
    }

}
