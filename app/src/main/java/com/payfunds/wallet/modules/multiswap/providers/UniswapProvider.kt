package com.payfunds.wallet.modules.multiswap.providers

import io.horizontalsystems.marketkit.models.BlockchainType
import com.payfunds.wallet.R

object UniswapProvider : BaseUniswapProvider() {
    override val id = "uniswap"
    override val title = "Uniswap"
    override val url = "https://uniswap.org/"
    override val icon = R.drawable.uniswap
    override val priority = 0

    override fun supports(blockchainType: BlockchainType): Boolean {
        return blockchainType == BlockchainType.Ethereum
    }
}
