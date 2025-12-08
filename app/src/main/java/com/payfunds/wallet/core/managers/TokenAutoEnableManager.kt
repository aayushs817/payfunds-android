package com.payfunds.wallet.core.managers

import io.horizontalsystems.marketkit.models.BlockchainType
import com.payfunds.wallet.core.storage.TokenAutoEnabledBlockchainDao
import com.payfunds.wallet.entities.Account
import com.payfunds.wallet.entities.TokenAutoEnabledBlockchain

class TokenAutoEnableManager(
    private val tokenAutoEnabledBlockchainDao: TokenAutoEnabledBlockchainDao
) {
    fun markAutoEnable(account: Account, blockchainType: BlockchainType) {
        tokenAutoEnabledBlockchainDao.insert(TokenAutoEnabledBlockchain(account.id, blockchainType))
    }

    fun isAutoEnabled(account: Account, blockchainType: BlockchainType): Boolean {
        return tokenAutoEnabledBlockchainDao.get(account.id, blockchainType) != null
    }
}
