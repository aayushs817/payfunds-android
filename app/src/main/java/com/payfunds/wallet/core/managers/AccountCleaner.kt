package com.payfunds.wallet.core.managers

import com.payfunds.wallet.core.IAccountCleaner
import com.payfunds.wallet.core.adapters.BinanceAdapter
import com.payfunds.wallet.core.adapters.BitcoinAdapter
import com.payfunds.wallet.core.adapters.BitcoinCashAdapter
import com.payfunds.wallet.core.adapters.DashAdapter
import com.payfunds.wallet.core.adapters.ECashAdapter
import com.payfunds.wallet.core.adapters.Eip20Adapter
import com.payfunds.wallet.core.adapters.EvmAdapter
import com.payfunds.wallet.core.adapters.SolanaAdapter
import com.payfunds.wallet.core.adapters.TronAdapter
import com.payfunds.wallet.core.adapters.zcash.ZcashAdapter

class AccountCleaner : IAccountCleaner {

    override fun clearAccounts(accountIds: List<String>) {
        accountIds.forEach { clearAccount(it) }
    }

    private fun clearAccount(accountId: String) {
        BinanceAdapter.clear(accountId)
        BitcoinAdapter.clear(accountId)
        BitcoinCashAdapter.clear(accountId)
        ECashAdapter.clear(accountId)
        DashAdapter.clear(accountId)
        EvmAdapter.clear(accountId)
        Eip20Adapter.clear(accountId)
        ZcashAdapter.clear(accountId)
        SolanaAdapter.clear(accountId)
        TronAdapter.clear(accountId)
    }

}
