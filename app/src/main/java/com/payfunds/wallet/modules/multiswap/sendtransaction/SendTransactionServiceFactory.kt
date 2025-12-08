package com.payfunds.wallet.modules.multiswap.sendtransaction

import com.payfunds.wallet.core.UnsupportedException
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.Token

object SendTransactionServiceFactory {
    fun create(token: Token): ISendTransactionService = when (token.blockchainType) {
        BlockchainType.Ethereum,
        BlockchainType.BinanceSmartChain,
        BlockchainType.Polygon,
        BlockchainType.Avalanche,
        BlockchainType.Optimism,
        BlockchainType.Base,
        BlockchainType.Gnosis,
        BlockchainType.Fantom,
        BlockchainType.ArbitrumOne -> SendTransactionServiceEvm(token.blockchainType)

        BlockchainType.Tron -> SendTransactionServiceTron(token)

        BlockchainType.Bitcoin,
        BlockchainType.BitcoinCash,
        BlockchainType.ECash,
        BlockchainType.Litecoin,
        BlockchainType.Dash,
        BlockchainType.Zcash,
        BlockchainType.BinanceChain,
        BlockchainType.Solana,
        BlockchainType.Ton,
        is BlockchainType.Unsupported -> throw UnsupportedException("")
    }
}
