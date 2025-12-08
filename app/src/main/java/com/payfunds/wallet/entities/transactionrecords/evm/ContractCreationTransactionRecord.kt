package com.payfunds.wallet.entities.transactionrecords.evm

import io.horizontalsystems.ethereumkit.models.Transaction
import io.horizontalsystems.marketkit.models.Token
import com.payfunds.wallet.modules.transactions.TransactionSource

class ContractCreationTransactionRecord(
    transaction: Transaction,
    baseToken: Token,
    source: TransactionSource
) : EvmTransactionRecord(transaction, baseToken, source)
