package com.payfunds.wallet.modules.transactions

import io.horizontalsystems.marketkit.models.Blockchain
import com.payfunds.wallet.core.Clearable
import com.payfunds.wallet.entities.transactionrecords.TransactionRecord
import com.payfunds.wallet.modules.contacts.model.Contact
import io.reactivex.Observable

interface ITransactionRecordRepository : Clearable {
    val itemsObservable: Observable<List<TransactionRecord>>

    fun set(
        transactionWallets: List<TransactionWallet>,
        wallet: TransactionWallet?,
        transactionType: FilterTransactionType,
        blockchain: Blockchain?,
        contact: Contact?
    )

    fun loadNext()
    fun reload()
}
