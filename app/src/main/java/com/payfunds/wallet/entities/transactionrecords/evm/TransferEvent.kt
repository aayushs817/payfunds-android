package com.payfunds.wallet.entities.transactionrecords.evm

import com.payfunds.wallet.entities.TransactionValue

data class TransferEvent(
    val address: String?,
    val value: TransactionValue
)
