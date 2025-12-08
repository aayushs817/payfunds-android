package com.payfunds.wallet.modules.multiswap.sendtransaction

import io.horizontalsystems.ethereumkit.models.TransactionData
import io.horizontalsystems.tronkit.models.TriggerSmartContract
import java.math.BigInteger

sealed class SendTransactionData {
    data class Evm(val transactionData: TransactionData, val gasLimit: Long?) :
        SendTransactionData()

    data class Tron(
        val contract: TriggerSmartContract,
        val feeLimit: Long? = null,
        val amountInScaled: BigInteger,
        val feeEstimate: com.payfunds.wallet.modules.multiswap.providers.tron.FeeEstimate? = null
    ) : SendTransactionData()
}
