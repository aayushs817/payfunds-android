package com.payfunds.wallet.modules.transactions

import com.payfunds.wallet.entities.ViewState

data class TransactionsUiState(
    val transactions: Map<String, List<TransactionViewItem>>?,
    val viewState: ViewState,
    val transactionListId: String?,
    val syncing: Boolean
)
