package com.payfunds.wallet.core.managers

import com.payfunds.wallet.core.ILocalStorage
import com.payfunds.wallet.entities.AppVersion
import io.payfunds.core.ISystemInfoManager
import java.util.Date

class AppVersionManager(
    private val systemInfoManager: ISystemInfoManager,
    private val localStorage: ILocalStorage
) {

    fun storeAppVersion() {
        val versions = localStorage.appVersions.toMutableList()
        val lastVersion = versions.lastOrNull()

        if (lastVersion == null || lastVersion.version != systemInfoManager.appVersion) {
            versions.add(AppVersion(systemInfoManager.appVersion, Date().time))
            localStorage.appVersions = versions
        }
    }

}
