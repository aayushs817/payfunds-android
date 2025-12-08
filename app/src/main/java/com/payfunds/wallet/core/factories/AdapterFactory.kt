package com.payfunds.wallet.core.factories

import android.content.Context
import android.util.Log
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.TokenQuery
import io.horizontalsystems.marketkit.models.TokenType
import io.horizontalsystems.tonkit.Address
import com.payfunds.wallet.core.IAdapter
import com.payfunds.wallet.core.ICoinManager
import com.payfunds.wallet.core.ILocalStorage
import com.payfunds.wallet.core.ITransactionsAdapter
import com.payfunds.wallet.core.adapters.BinanceAdapter
import com.payfunds.wallet.core.adapters.BitcoinAdapter
import com.payfunds.wallet.core.adapters.BitcoinCashAdapter
import com.payfunds.wallet.core.adapters.DashAdapter
import com.payfunds.wallet.core.adapters.ECashAdapter
import com.payfunds.wallet.core.adapters.Eip20Adapter
import com.payfunds.wallet.core.adapters.EvmAdapter
import com.payfunds.wallet.core.adapters.EvmTransactionsAdapter
import com.payfunds.wallet.core.adapters.JettonAdapter
import com.payfunds.wallet.core.adapters.LitecoinAdapter
import com.payfunds.wallet.core.adapters.SolanaAdapter
import com.payfunds.wallet.core.adapters.SolanaTransactionConverter
import com.payfunds.wallet.core.adapters.SolanaTransactionsAdapter
import com.payfunds.wallet.core.adapters.SplAdapter
import com.payfunds.wallet.core.adapters.TonAdapter
import com.payfunds.wallet.core.adapters.TonTransactionConverter
import com.payfunds.wallet.core.adapters.TonTransactionsAdapter
import com.payfunds.wallet.core.adapters.Trc20Adapter
import com.payfunds.wallet.core.adapters.TronAdapter
import com.payfunds.wallet.core.adapters.TronTransactionConverter
import com.payfunds.wallet.core.adapters.TronTransactionsAdapter
import com.payfunds.wallet.core.adapters.zcash.ZcashAdapter
import com.payfunds.wallet.core.managers.BinanceKitManager
import com.payfunds.wallet.core.managers.BtcBlockchainManager
import com.payfunds.wallet.core.managers.EvmBlockchainManager
import com.payfunds.wallet.core.managers.EvmLabelManager
import com.payfunds.wallet.core.managers.EvmSyncSourceManager
import com.payfunds.wallet.core.managers.RestoreSettingsManager
import com.payfunds.wallet.core.managers.SolanaKitManager
import com.payfunds.wallet.core.managers.TonKitManager
import com.payfunds.wallet.core.managers.TronKitManager
import com.payfunds.wallet.entities.Wallet
import com.payfunds.wallet.modules.transactions.TransactionSource
import io.payfunds.core.BackgroundManager

class AdapterFactory(
    private val context: Context,
    private val btcBlockchainManager: BtcBlockchainManager,
    private val evmBlockchainManager: EvmBlockchainManager,
    private val evmSyncSourceManager: EvmSyncSourceManager,
    private val binanceKitManager: BinanceKitManager,
    private val solanaKitManager: SolanaKitManager,
    private val tronKitManager: TronKitManager,
    private val tonKitManager: TonKitManager,
    private val backgroundManager: BackgroundManager,
    private val restoreSettingsManager: RestoreSettingsManager,
    private val coinManager: ICoinManager,
    private val evmLabelManager: EvmLabelManager,
    private val localStorage: ILocalStorage,
) {

    private fun getEvmAdapter(wallet: Wallet): IAdapter? {
        val blockchainType = evmBlockchainManager.getBlockchain(wallet.token)?.type ?: return null
        val evmKitWrapper = evmBlockchainManager.getEvmKitManager(blockchainType).getEvmKitWrapper(
            wallet.account,
            blockchainType
        )

        return EvmAdapter(evmKitWrapper, coinManager)
    }

    private fun getEip20Adapter(wallet: Wallet, address: String): IAdapter? {
        val blockchainType = evmBlockchainManager.getBlockchain(wallet.token)?.type ?: return null
        val evmKitWrapper = evmBlockchainManager.getEvmKitManager(blockchainType)
            .getEvmKitWrapper(wallet.account, blockchainType)
        val baseToken = evmBlockchainManager.getBaseToken(blockchainType) ?: return null

        return Eip20Adapter(
            context,
            evmKitWrapper,
            address,
            baseToken,
            coinManager,
            wallet,
            evmLabelManager
        )
    }

    private fun getSplAdapter(wallet: Wallet, address: String): IAdapter {
        val solanaKitWrapper = solanaKitManager.getSolanaKitWrapper(wallet.account)

        return SplAdapter(solanaKitWrapper, wallet, address)
    }

    private fun getTrc20Adapter(wallet: Wallet, address: String): IAdapter {
        val tronKitWrapper = tronKitManager.getTronKitWrapper(wallet.account)

        return Trc20Adapter(tronKitWrapper, address, wallet)
    }

    private fun getJettonAdapter(wallet: Wallet, address: String): IAdapter {
        val tonKitWrapper = tonKitManager.getTonKitWrapper(wallet.account)

        return JettonAdapter(tonKitWrapper, address, wallet)
    }

    fun getAdapterOrNull(wallet: Wallet) = try {
        getAdapter(wallet)
    } catch (e: Throwable) {
        Log.e("AAA", "get adapter error", e)
        null
    }

    private fun getAdapter(wallet: Wallet) = when (val tokenType = wallet.token.type) {
        is TokenType.Derived -> {
            when (wallet.token.blockchainType) {
                BlockchainType.Bitcoin -> null
//                    {
//                    val syncMode =
//                        btcBlockchainManager.syncMode(BlockchainType.Bitcoin, wallet.account.origin)
//                    BitcoinAdapter(wallet, syncMode, backgroundManager, tokenType.derivation)
//                }

                BlockchainType.Litecoin -> {
                    val syncMode = btcBlockchainManager.syncMode(
                        BlockchainType.Litecoin,
                        wallet.account.origin
                    )
                    LitecoinAdapter(wallet, syncMode, backgroundManager, tokenType.derivation)
                }

                else -> null
            }
        }

        is TokenType.AddressTyped -> {
            if (wallet.token.blockchainType == BlockchainType.BitcoinCash) {
                val syncMode =
                    btcBlockchainManager.syncMode(BlockchainType.BitcoinCash, wallet.account.origin)
                BitcoinCashAdapter(wallet, syncMode, backgroundManager, tokenType.type)
            } else null
        }

        TokenType.Native -> when (wallet.token.blockchainType) {
            BlockchainType.ECash -> {
                val syncMode =
                    btcBlockchainManager.syncMode(BlockchainType.ECash, wallet.account.origin)
                ECashAdapter(wallet, syncMode, backgroundManager)
            }

            BlockchainType.Dash -> {
                val syncMode =
                    btcBlockchainManager.syncMode(BlockchainType.Dash, wallet.account.origin)
                DashAdapter(wallet, syncMode, backgroundManager)
            }

            BlockchainType.Zcash -> {
                ZcashAdapter(
                    context,
                    wallet,
                    restoreSettingsManager.settings(wallet.account, wallet.token.blockchainType),
                    localStorage
                )
            }

            BlockchainType.Ethereum,
            BlockchainType.BinanceSmartChain,
            BlockchainType.Polygon,
            BlockchainType.Avalanche,
            BlockchainType.Optimism,
            BlockchainType.Base,
            BlockchainType.Gnosis,
            BlockchainType.Fantom,
            BlockchainType.ArbitrumOne -> {
                getEvmAdapter(wallet)
            }

            BlockchainType.BinanceChain -> {
                getBinanceAdapter(wallet, "BNB")
            }

            BlockchainType.Solana -> {
                val solanaKitWrapper = solanaKitManager.getSolanaKitWrapper(wallet.account)
                SolanaAdapter(solanaKitWrapper)
            }

            BlockchainType.Tron -> {
                TronAdapter(tronKitManager.getTronKitWrapper(wallet.account))
            }

            BlockchainType.Ton -> {
                TonAdapter(tonKitManager.getTonKitWrapper(wallet.account))
            }

            else -> null
        }

        is TokenType.Eip20 -> {
            if (wallet.token.blockchainType == BlockchainType.Tron) {
                getTrc20Adapter(wallet, tokenType.address)
            } else {
                getEip20Adapter(wallet, tokenType.address)
            }
        }

        is TokenType.Bep2 -> getBinanceAdapter(wallet, tokenType.symbol)
        is TokenType.Spl -> getSplAdapter(wallet, tokenType.address)
        is TokenType.Jetton -> getJettonAdapter(wallet, tokenType.address)
        is TokenType.Unsupported -> null
    }

    private fun getBinanceAdapter(
        wallet: Wallet,
        symbol: String
    ): BinanceAdapter? {
        val query = TokenQuery(BlockchainType.BinanceChain, TokenType.Native)
        return coinManager.getToken(query)?.let { feeToken ->
            BinanceAdapter(binanceKitManager.binanceKit(wallet), symbol, feeToken, wallet)
        }
    }

    fun evmTransactionsAdapter(
        source: TransactionSource,
        blockchainType: BlockchainType
    ): ITransactionsAdapter? {
        val evmKitWrapper = evmBlockchainManager.getEvmKitManager(blockchainType)
            .getEvmKitWrapper(source.account, blockchainType)
        val baseCoin = evmBlockchainManager.getBaseToken(blockchainType) ?: return null
        val syncSource = evmSyncSourceManager.getSyncSource(blockchainType)

        return EvmTransactionsAdapter(
            evmKitWrapper,
            baseCoin,
            coinManager,
            source,
            syncSource.transactionSource,
            evmLabelManager
        )
    }

    fun solanaTransactionsAdapter(source: TransactionSource): ITransactionsAdapter? {
        val solanaKitWrapper = solanaKitManager.getSolanaKitWrapper(source.account)
        val baseToken =
            coinManager.getToken(TokenQuery(BlockchainType.Solana, TokenType.Native)) ?: return null
        val solanaTransactionConverter =
            SolanaTransactionConverter(coinManager, source, baseToken, solanaKitWrapper)

        return SolanaTransactionsAdapter(solanaKitWrapper, solanaTransactionConverter)
    }

    fun tronTransactionsAdapter(source: TransactionSource): ITransactionsAdapter? {
        val tronKitWrapper = tronKitManager.getTronKitWrapper(source.account)
        val baseToken =
            coinManager.getToken(TokenQuery(BlockchainType.Tron, TokenType.Native)) ?: return null
        val tronTransactionConverter = TronTransactionConverter(
            coinManager,
            tronKitWrapper,
            source,
            baseToken,
            evmLabelManager
        )

        return TronTransactionsAdapter(tronKitWrapper, tronTransactionConverter)
    }

    fun tonTransactionsAdapter(source: TransactionSource): ITransactionsAdapter? {
        val tonKitWrapper = tonKitManager.getTonKitWrapper(source.account)
        val address = tonKitWrapper.tonKit.receiveAddress

        val tonTransactionConverter = tonTransactionConverter(address, source) ?: return null

        return TonTransactionsAdapter(tonKitWrapper, tonTransactionConverter)
    }

    fun tonTransactionConverter(
        address: Address,
        source: TransactionSource,
    ): TonTransactionConverter? {
        val query = TokenQuery(BlockchainType.Ton, TokenType.Native)
        val baseToken = coinManager.getToken(query) ?: return null
        return TonTransactionConverter(
            address,
            coinManager,
            source,
            baseToken
        )
    }

    fun unlinkAdapter(wallet: Wallet) {
        when (val blockchainType = wallet.transactionSource.blockchain.type) {
            BlockchainType.Ethereum,
            BlockchainType.BinanceSmartChain,
            BlockchainType.Polygon,
            BlockchainType.Optimism,
            BlockchainType.Base,
            BlockchainType.ArbitrumOne -> {
                val evmKitManager = evmBlockchainManager.getEvmKitManager(blockchainType)
                evmKitManager.unlink(wallet.account)
            }

            BlockchainType.BinanceChain -> {
                binanceKitManager.unlink(wallet.account)
            }

            BlockchainType.Solana -> {
                solanaKitManager.unlink(wallet.account)
            }

            BlockchainType.Tron -> {
                tronKitManager.unlink(wallet.account)
            }

            BlockchainType.Ton -> {
                tonKitManager.unlink(wallet.account)
            }

            else -> Unit
        }
    }

    fun unlinkAdapter(transactionSource: TransactionSource) {
        when (val blockchainType = transactionSource.blockchain.type) {
            BlockchainType.Ethereum,
            BlockchainType.BinanceSmartChain,
            BlockchainType.Polygon,
            BlockchainType.Optimism,
            BlockchainType.Base,
            BlockchainType.ArbitrumOne -> {
                val evmKitManager = evmBlockchainManager.getEvmKitManager(blockchainType)
                evmKitManager.unlink(transactionSource.account)
            }

            BlockchainType.Solana -> {
                solanaKitManager.unlink(transactionSource.account)
            }

            BlockchainType.Tron -> {
                tronKitManager.unlink(transactionSource.account)
            }

            BlockchainType.Ton -> {
                tonKitManager.unlink(transactionSource.account)
            }

            else -> Unit
        }
    }
}
