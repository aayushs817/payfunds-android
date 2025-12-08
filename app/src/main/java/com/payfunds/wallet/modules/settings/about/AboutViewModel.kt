package com.payfunds.wallet.modules.settings.about

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.payfunds.wallet.R
import com.payfunds.wallet.core.ITermsManager
import com.payfunds.wallet.core.providers.AppConfigProvider
import com.payfunds.wallet.core.providers.Translator
import io.payfunds.core.ISystemInfoManager
import kotlinx.coroutines.launch

class AboutViewModel(
    private val appConfigProvider: AppConfigProvider,
    private val termsManager: ITermsManager,
    private val systemInfoManager: ISystemInfoManager,
) : ViewModel() {

    val appFacebookLink = appConfigProvider.appFacebookLink
    val appInstagramLink = appConfigProvider.appInstagramLink
    val appRedditLink = appConfigProvider.appRedditLink
    val appTelegramLink = appConfigProvider.appTelegramLink
    val appWebPageLink = appConfigProvider.appWebPageLink
    val privacyPolicyLink = appConfigProvider.privacyPolicyLink
    val appXLink = appConfigProvider.appXLink
    val appYoutubeLink = appConfigProvider.appYoutubeLink

    val appVersion: String
        get() {
            var appVersion = systemInfoManager.appVersion
            if (Translator.getString(R.string.is_release) == "false") {
                appVersion += " (${appConfigProvider.appBuild})"
            }

            return appVersion
        }

    var termsShowAlert by mutableStateOf(!termsManager.allTermsAccepted)
        private set

    init {
        viewModelScope.launch {
            termsManager.termsAcceptedSignalFlow.collect {
                termsShowAlert = !it
            }
        }
    }

}
