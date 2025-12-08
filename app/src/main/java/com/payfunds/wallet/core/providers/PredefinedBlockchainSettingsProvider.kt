package com.payfunds.wallet.core.providers

import io.horizontalsystems.marketkit.models.BlockchainType
import com.payfunds.wallet.core.managers.RestoreSettings
import com.payfunds.wallet.core.managers.RestoreSettingsManager
import com.payfunds.wallet.core.managers.ZcashBirthdayProvider
import com.payfunds.wallet.entities.Account

class PredefinedBlockchainSettingsProvider(
    private val manager: RestoreSettingsManager,
    private val zcashBirthdayProvider: ZcashBirthdayProvider
) {

    fun prepareNew(account: Account, blockchainType: BlockchainType) {
        val settings = RestoreSettings()
        when (blockchainType) {
            BlockchainType.Zcash -> {
                settings.birthdayHeight = zcashBirthdayProvider.getLatestCheckpointBlockHeight()
            }

            else -> {}
        }
        if (settings.isNotEmpty()) {
            manager.save(settings, account, blockchainType)
        }
    }
}
