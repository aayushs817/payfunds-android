package com.payfunds.wallet.core.adapters

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import com.payfunds.wallet.core.AdapterState
import com.payfunds.wallet.core.App.Companion.adapterManager
import com.payfunds.wallet.core.BalanceData
import com.payfunds.wallet.core.ISendTronAdapter
import com.payfunds.wallet.core.managers.TronKitWrapper
import com.payfunds.wallet.entities.Wallet
import com.payfunds.wallet.modules.balance.tron.TronBalanceService
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.tronkit.TronKit.SyncState
import io.horizontalsystems.tronkit.models.Address
import io.horizontalsystems.tronkit.transaction.Fee
import io.reactivex.Flowable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.asFlowable
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.BigInteger

class Trc20Adapter(
    tronKitWrapper: TronKitWrapper,
    val mContractAddress: String,
    private val wallet: Wallet,
) : BaseTronAdapter(tronKitWrapper, wallet.decimal), ISendTronAdapter {

    val contractAddress: Address
        get() = Address.fromBase58(mContractAddress)

    override val ownAddress: String
        get() = receiveAddress

    override val contractAddressHex: String
        get() = contractAddress.hex

    override val tokenSymbol: String
        get() = wallet.token.coin.code

    private val tronBalanceService = TronBalanceService()

    private var tronBalance = mutableStateOf("0")
    private var isActiveWallet = mutableStateOf(true)

    // IAdapter

    override fun start() {
        // started via TronKitManager
        getTronBalance()
        checkIsActiveWallet()
    }

    override fun stop() {
        // stopped via TronKitManager
    }

    override fun refresh() {
        getTronBalance()
        checkIsActiveWallet()
        // refreshed via TronKitManager
    }

    // IBalanceAdapter

    override val balanceState: AdapterState
        get() = convertToAdapterState(tronKit.syncState)

    override val balanceStateUpdatedFlowable: Flowable<Unit>
        get() = tronKit.syncStateFlow.map { }.asFlowable()


    override val balanceData: BalanceData
        get() = if (isActiveWallet.value) {
            BalanceData(
                balanceInBigDecimal(
                    tronKit.getTrc20Balance(contractAddress.base58),
                    decimal
                )
            )
        } else {
            BalanceData(
                balanceInBigDecimal(
                    BigInteger(tronBalance.value),
                    decimal
                )
            )
        }

    private fun checkIsActiveWallet() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (wallet.token.blockchainType is BlockchainType.Tron) {
                    (adapterManager.getAdapterForWallet(wallet) as? BaseTronAdapter)?.let { adapter ->
                        isActiveWallet.value = adapter.isAddressActive(adapter.receiveAddress)
                    }
                } else {
                    isActiveWallet.value = false
                }
            } catch (e: Exception) {
                Log.i("Failed", e.message.toString())
            }
        }
    }

    private fun getTronBalance() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tronBalanceResponse =
                    tronBalanceService.fetchTronAccountData()
                val rowBalance =
                    tronBalanceResponse?.withPriceTokens?.first { it.tokenId == contractAddress.base58 }?.balance
                tronBalance.value = rowBalance ?: "0"

            } catch (e: Exception) {
                Log.i("Failed", e.message.toString())
            }
        }
    }


    override val balanceUpdatedFlowable: Flowable<Unit>
        get() = tronKit.getTrc20BalanceFlow(contractAddress.base58).map { }.asFlowable()

    // ISendTronAdapter

    override val trxBalanceData: BalanceData
        get() = BalanceData(balanceInBigDecimal(tronKit.trxBalance, TronAdapter.decimal))

    override suspend fun estimateFee(amount: BigDecimal, to: Address): List<Fee> =
        withContext(Dispatchers.IO) {
            val amountBigInt = amount.movePointRight(decimal).toBigInteger()
            val contract =
                tronKit.transferTrc20TriggerSmartContract(contractAddress, to, amountBigInt)
            tronKit.estimateFee(contract)
        }

    override suspend fun send(amount: BigDecimal, to: Address, feeLimit: Long?) : String {
        if (signer == null) throw Exception()
        val amountBigInt = amount.movePointRight(decimal).toBigInteger()
        val contract = tronKit.transferTrc20TriggerSmartContract(contractAddress, to, amountBigInt)

        return tronKit.send(contract, signer, feeLimit)
    }

    private fun convertToAdapterState(syncState: SyncState): AdapterState = when (syncState) {
        is SyncState.Synced -> AdapterState.Synced
        is SyncState.NotSynced -> AdapterState.NotSynced(syncState.error)
        is SyncState.Syncing -> AdapterState.Syncing()
    }

}
