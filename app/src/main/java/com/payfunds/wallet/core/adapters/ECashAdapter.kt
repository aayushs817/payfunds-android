package com.payfunds.wallet.core.adapters

import io.horizontalsystems.bitcoincore.BitcoinCore
import io.horizontalsystems.bitcoincore.models.BalanceInfo
import io.horizontalsystems.bitcoincore.models.BlockInfo
import io.horizontalsystems.bitcoincore.models.TransactionInfo
import io.horizontalsystems.bitcoincore.storage.UnspentOutputInfo
import io.horizontalsystems.bitcoincore.storage.UtxoFilters
import io.horizontalsystems.ecash.ECashKit
import io.horizontalsystems.marketkit.models.BlockchainType
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.ISendBitcoinAdapter
import com.payfunds.wallet.core.UnsupportedAccountException
import com.payfunds.wallet.core.UsedAddress
import com.payfunds.wallet.entities.AccountType
import com.payfunds.wallet.entities.Wallet
import com.payfunds.wallet.entities.transactionrecords.TransactionRecord
import io.payfunds.core.BackgroundManager
import java.math.BigDecimal

class ECashAdapter(
    override val kit: ECashKit,
    syncMode: BitcoinCore.SyncMode,
    backgroundManager: BackgroundManager,
    wallet: Wallet,
) : BitcoinBaseAdapter(kit, syncMode, backgroundManager, wallet, confirmationsThreshold, 2),
    ECashKit.Listener, ISendBitcoinAdapter {

    constructor(
        wallet: Wallet,
        syncMode: BitcoinCore.SyncMode,
        backgroundManager: BackgroundManager
    ) : this(createKit(wallet, syncMode), syncMode, backgroundManager, wallet)

    init {
        kit.listener = this
    }

    //
    // BitcoinBaseAdapter
    //

    override val satoshisInBitcoin: BigDecimal =
        BigDecimal.valueOf(Math.pow(10.0, decimal.toDouble()))

    //
    // ECashKit Listener
    //

    override val explorerTitle: String
        get() = "blockchair.com"

    override fun getTransactionUrl(transactionHash: String): String =
        "https://blockchair.com/ecash/transaction/$transactionHash"

    override fun onBalanceUpdate(balance: BalanceInfo) {
        balanceUpdatedSubject.onNext(Unit)
    }

    override fun onLastBlockInfoUpdate(blockInfo: BlockInfo) {
        lastBlockUpdatedSubject.onNext(Unit)
    }

    override fun onKitStateUpdate(state: BitcoinCore.KitState) {
        setState(state)
    }

    override fun onTransactionsUpdate(
        inserted: List<TransactionInfo>,
        updated: List<TransactionInfo>
    ) {
        val records = mutableListOf<TransactionRecord>()

        for (info in inserted) {
            records.add(transactionRecord(info))
        }

        for (info in updated) {
            records.add(transactionRecord(info))
        }

        transactionRecordsSubject.onNext(records)
    }

    override fun onTransactionsDelete(hashes: List<String>) {
        // ignored for now
    }

    override val unspentOutputs: List<UnspentOutputInfo>
        get() = kit.getUnspentOutputs(UtxoFilters())

    override val blockchainType = BlockchainType.ECash

    override fun usedAddresses(change: Boolean): List<UsedAddress> =
        kit.usedAddresses(change).map {
            UsedAddress(
                it.index,
                it.address,
                "https://blockchair.com/ecash/address/${it.address}"
            )
        }

    companion object {
        private const val confirmationsThreshold = 1

        private fun createKit(wallet: Wallet, syncMode: BitcoinCore.SyncMode): ECashKit {
            val account = wallet.account
            when (val accountType = account.type) {
                is AccountType.HdExtendedKey -> {
                    return ECashKit(
                        context = App.instance,
                        extendedKey = accountType.hdExtendedKey,
                        walletId = account.id,
                        syncMode = syncMode,
                        networkType = ECashKit.NetworkType.MainNet,
                        confirmationsThreshold = confirmationsThreshold
                    )
                }

                is AccountType.Mnemonic -> {
                    return ECashKit(
                        context = App.instance,
                        words = accountType.words,
                        passphrase = accountType.passphrase,
                        walletId = account.id,
                        syncMode = syncMode,
                        networkType = ECashKit.NetworkType.MainNet,
                        confirmationsThreshold = confirmationsThreshold
                    )
                }

                is AccountType.BitcoinAddress -> {
                    return ECashKit(
                        context = App.instance,
                        watchAddress = accountType.address,
                        walletId = account.id,
                        syncMode = syncMode,
                        networkType = ECashKit.NetworkType.MainNet,
                        confirmationsThreshold = confirmationsThreshold
                    )
                }

                else -> throw UnsupportedAccountException()
            }

        }

        fun clear(walletId: String) {
            ECashKit.clear(App.instance, ECashKit.NetworkType.MainNet, walletId)
        }
    }
}
