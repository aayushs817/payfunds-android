package com.payfunds.wallet.core.adapters

import io.horizontalsystems.solanakit.Signer
import com.payfunds.wallet.core.IAdapter
import com.payfunds.wallet.core.IBalanceAdapter
import com.payfunds.wallet.core.IReceiveAdapter
import com.payfunds.wallet.core.managers.SolanaKitWrapper

abstract class BaseSolanaAdapter(
    solanaKitWrapper: SolanaKitWrapper,
    val decimal: Int
) : IAdapter, IBalanceAdapter, IReceiveAdapter {

    val solanaKit = solanaKitWrapper.solanaKit
    protected val signer: Signer? = solanaKitWrapper.signer

    override val debugInfo: String
        get() = solanaKit.debugInfo()

    val statusInfo: Map<String, Any>
        get() = solanaKit.statusInfo()

    // IReceiveAdapter

    override val receiveAddress: String
        get() = solanaKit.receiveAddress

    override val isMainNet: Boolean
        get() = solanaKit.isMainnet

    companion object {
        const val confirmationsThreshold: Int = 12
    }

}
