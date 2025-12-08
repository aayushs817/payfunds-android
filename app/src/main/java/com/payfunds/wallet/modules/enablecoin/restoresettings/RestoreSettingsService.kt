package com.payfunds.wallet.modules.enablecoin.restoresettings

import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.Token
import com.payfunds.wallet.core.Clearable
import com.payfunds.wallet.core.managers.RestoreSettingType
import com.payfunds.wallet.core.managers.RestoreSettings
import com.payfunds.wallet.core.managers.RestoreSettingsManager
import com.payfunds.wallet.core.managers.ZcashBirthdayProvider
import com.payfunds.wallet.core.restoreSettingTypes
import com.payfunds.wallet.entities.Account
import com.payfunds.wallet.entities.AccountOrigin
import io.reactivex.subjects.PublishSubject

class RestoreSettingsService(
    private val manager: RestoreSettingsManager,
    private val zcashBirthdayProvider: ZcashBirthdayProvider
) : Clearable {

    val approveSettingsObservable = PublishSubject.create<TokenWithSettings>()
    val rejectApproveSettingsObservable = PublishSubject.create<Token>()
    val requestObservable = PublishSubject.create<Request>()

    fun approveSettings(token: Token, account: Account? = null) {
        val blockchainType = token.blockchainType

        if (account != null && account.origin == AccountOrigin.Created) {
            val settings = RestoreSettings()
            blockchainType.restoreSettingTypes.forEach { settingType ->
                manager.getSettingValueForCreatedAccount(settingType, blockchainType)?.let {
                    settings[settingType] = it
                }
            }
            approveSettingsObservable.onNext(TokenWithSettings(token, settings))
            return
        }

        val existingSettings =
            account?.let { manager.settings(it, blockchainType) } ?: RestoreSettings()

        if (blockchainType.restoreSettingTypes.contains(RestoreSettingType.BirthdayHeight)
            && existingSettings.birthdayHeight == null
        ) {
            requestObservable.onNext(Request(token, RequestType.BirthdayHeight))
            return
        }

        approveSettingsObservable.onNext(TokenWithSettings(token, RestoreSettings()))
    }

    fun save(settings: RestoreSettings, account: Account, blockchainType: BlockchainType) {
        manager.save(settings, account, blockchainType)
    }

    fun enter(zcashConfig: ZCashConfig, token: Token) {
        val settings = RestoreSettings()
        settings.birthdayHeight =
            if (zcashConfig.restoreAsNew)
                zcashBirthdayProvider.getLatestCheckpointBlockHeight()
            else
                zcashConfig.birthdayHeight?.toLongOrNull()

        val tokenWithSettings = TokenWithSettings(token, settings)
        approveSettingsObservable.onNext(tokenWithSettings)
    }

    fun cancel(token: Token) {
        rejectApproveSettingsObservable.onNext(token)
    }

    override fun clear() = Unit

    data class TokenWithSettings(val token: Token, val settings: RestoreSettings)
    data class Request(val token: Token, val requestType: RequestType)
    enum class RequestType {
        BirthdayHeight
    }
}
