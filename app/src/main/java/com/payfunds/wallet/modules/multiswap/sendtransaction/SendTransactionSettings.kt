package com.payfunds.wallet.modules.multiswap.sendtransaction

import io.horizontalsystems.ethereumkit.models.Address
import com.payfunds.wallet.modules.evmfee.GasPriceInfo
import io.horizontalsystems.tronkit.models.Address as TronAddress

sealed class SendTransactionSettings {
    data class Evm(val gasPriceInfo: GasPriceInfo?, val receiveAddress: Address) :
        SendTransactionSettings()

    data class Tron(val ownerAddress: TronAddress) : SendTransactionSettings()
}
