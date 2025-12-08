package com.payfunds.wallet.core.adapters

import io.horizontalsystems.tonkit.models.Network
import com.payfunds.wallet.core.IAdapter
import com.payfunds.wallet.core.IBalanceAdapter
import com.payfunds.wallet.core.IReceiveAdapter
import com.payfunds.wallet.core.managers.TonKitWrapper
import com.payfunds.wallet.core.managers.statusInfo

abstract class BaseTonAdapter(
    tonKitWrapper: TonKitWrapper,
    val decimals: Int
) : IAdapter, IBalanceAdapter, IReceiveAdapter {

    val tonKit = tonKitWrapper.tonKit

    override val debugInfo: String
        get() = ""

    val statusInfo: Map<String, Any>
        get() = tonKit.statusInfo()

    // IReceiveAdapter

    override val receiveAddress: String
        get() = tonKit.receiveAddress.toUserFriendly(false)

    override val isMainNet: Boolean
        get() = tonKit.network == Network.MainNet

    // ISendTronAdapter
}
