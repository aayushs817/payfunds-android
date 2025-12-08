package com.payfunds.wallet.core.managers

import io.horizontalsystems.marketkit.models.BlockchainType
import com.payfunds.wallet.core.IAdapter
import com.payfunds.wallet.core.IAdapterManager
import com.payfunds.wallet.core.ITransactionsAdapter
import com.payfunds.wallet.core.factories.AdapterFactory
import com.payfunds.wallet.entities.Wallet
import com.payfunds.wallet.modules.transactions.TransactionSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow
import java.util.concurrent.ConcurrentHashMap

class TransactionAdapterManager(
    private val adapterManager: IAdapterManager,
    private val adapterFactory: AdapterFactory
) {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    private val _adaptersReadyFlow =
        MutableSharedFlow<Map<TransactionSource, ITransactionsAdapter>>(
            replay = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST
        )
    val adaptersReadyFlow get() = _adaptersReadyFlow.asSharedFlow()

    val adaptersMap = ConcurrentHashMap<TransactionSource, ITransactionsAdapter>()

    init {
        coroutineScope.launch {
            adapterManager.adaptersReadyObservable.asFlow().collect(::initAdapters)
        }
    }

    fun getAdapter(source: TransactionSource): ITransactionsAdapter? = adaptersMap[source]

    private fun initAdapters(adaptersMap: Map<Wallet, IAdapter>) {
        val currentAdapters = this.adaptersMap.toMutableMap()
        this.adaptersMap.clear()

        for ((wallet, adapter) in adaptersMap) {
            val source = wallet.transactionSource
            if (this.adaptersMap.containsKey(source)) continue

            var txAdapter = currentAdapters.remove(source)
            if (txAdapter == null) {
                txAdapter = when (val blockchainType = source.blockchain.type) {
                    BlockchainType.Ethereum,
                    BlockchainType.BinanceSmartChain,
                    BlockchainType.Polygon,
                    BlockchainType.Avalanche,
                    BlockchainType.Optimism,
                    BlockchainType.Base,
                    BlockchainType.Gnosis,
                    BlockchainType.Fantom,
                    BlockchainType.ArbitrumOne -> {
                        adapterFactory.evmTransactionsAdapter(
                            wallet.transactionSource,
                            blockchainType
                        )
                    }

                    BlockchainType.Solana -> {
                        adapterFactory.solanaTransactionsAdapter(wallet.transactionSource)
                    }

                    BlockchainType.Tron -> {
                        adapterFactory.tronTransactionsAdapter(wallet.transactionSource)
                    }

                    BlockchainType.Ton -> {
                        adapterFactory.tonTransactionsAdapter(wallet.transactionSource)
                    }

                    else -> adapter as? ITransactionsAdapter
                }
            }

            txAdapter?.let {
                this.adaptersMap[source] = it
            }
        }

        currentAdapters.forEach {
            adapterFactory.unlinkAdapter(it.key)
        }

        _adaptersReadyFlow.tryEmit(this.adaptersMap)
    }
}
