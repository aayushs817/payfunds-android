package com.payfunds.wallet.modules.multiswap.providers

import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.uniswapkit.models.DexType
import com.payfunds.wallet.R

object PancakeSwapV3Provider : BaseUniswapV3Provider(DexType.PancakeSwap) {
    override val id = "pancake_v3"
    override val title = "PancakeSwap V3"
    override val url = "https://pancakeswap.finance/"
    override val icon = R.drawable.pancake_v3
    override val priority = 0

    override fun supports(blockchainType: BlockchainType) = when (blockchainType) {
        BlockchainType.BinanceSmartChain,
        BlockchainType.Ethereum
            -> true

        else -> false
    }
}
