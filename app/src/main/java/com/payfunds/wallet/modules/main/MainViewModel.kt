package com.payfunds.wallet.modules.main

import android.net.Uri
import android.util.Log
import androidx.lifecycle.viewModelScope
import cash.z.ecc.android.sdk.ext.collectWith
import io.horizontalsystems.ethereumkit.models.Chain
import com.payfunds.wallet.R
import com.payfunds.wallet.core.DashboardObject
import com.payfunds.wallet.core.IAccountManager
import com.payfunds.wallet.core.IBackupManager
import com.payfunds.wallet.core.ILocalStorage
import com.payfunds.wallet.core.INetworkManager
import com.payfunds.wallet.core.IRateAppManager
import com.payfunds.wallet.core.ITermsManager
import com.payfunds.wallet.core.ViewModelUiState
import com.payfunds.wallet.core.managers.ActiveAccountState
import com.payfunds.wallet.core.managers.ReleaseNotesManager
import com.payfunds.wallet.core.providers.Translator
import com.payfunds.wallet.core.stats.StatEvent
import com.payfunds.wallet.core.stats.StatPage
import com.payfunds.wallet.core.stats.stat
import com.payfunds.wallet.entities.Account
import com.payfunds.wallet.entities.AccountType
import com.payfunds.wallet.entities.LaunchPage
import com.payfunds.wallet.modules.coin.CoinFragment
import com.payfunds.wallet.modules.main.MainModule.MainNavigation
import com.payfunds.wallet.modules.market.topplatforms.Platform
import com.payfunds.wallet.modules.nft.collection.NftCollectionFragment
import com.payfunds.wallet.modules.walletconnect.WCManager
import com.payfunds.wallet.modules.walletconnect.WCSessionManager
import com.payfunds.wallet.modules.walletconnect.list.WCListFragment
import io.payfunds.core.IPinComponent
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow

class MainViewModel(
    private val pinComponent: IPinComponent,
    rateAppManager: IRateAppManager,
    private val backupManager: IBackupManager,
    private val termsManager: ITermsManager,
    private val accountManager: IAccountManager,
    private val releaseNotesManager: ReleaseNotesManager,
    private val localStorage: ILocalStorage,
    wcSessionManager: WCSessionManager,
    private val wcManager: WCManager,
    private val networkManager: INetworkManager,
) : ViewModelUiState<MainModule.UiState>() {

    private var wcPendingRequestsCount = 0

    private var marketsTabEnabled = localStorage.marketsTabEnabledFlow.value

    private var transactionsEnabled = isTransactionsTabEnabled()
    private var settingsBadge: MainModule.BadgeType? = null
    private val launchPage: LaunchPage
        get() = localStorage.launchPage ?: LaunchPage.Auto

    private var currentMainTab: MainNavigation
        get() = localStorage.mainTab ?: MainNavigation.Home
        set(value) {
            localStorage.mainTab = value
        }

    private var relaunchBySettingChange: Boolean
        get() = localStorage.relaunchBySettingChange
        set(value) {
            localStorage.relaunchBySettingChange = value
        }

    private val items: List<MainNavigation>
        get() = if (marketsTabEnabled) {
            listOf(
                MainNavigation.Home,
                MainNavigation.Market,
                MainNavigation.Transactions,
                MainNavigation.Settings,
            )
        } else {
            listOf(
                MainNavigation.Home,
                MainNavigation.Transactions,
                MainNavigation.Settings,
            )
        }

    private var selectedTabIndex =
        if (DashboardObject.notificationWalletAddress == null) getTabIndexToOpen() else 1
    private var deeplinkPage: DeeplinkPage? = null
    private var mainNavItems = navigationItems()
    private var showRateAppDialog = false
    private var contentHidden = pinComponent.isLocked

    //    private var showWhatsNew = false
    private var activeWallet = accountManager.activeAccount
    private var wcSupportState: WCManager.SupportState? = null
    private var torEnabled = localStorage.torEnabled

    val wallets: List<Account>
        get() = accountManager.accounts.filter { !it.isWatchAccount }

    val watchWallets: List<Account>
        get() = accountManager.accounts.filter { it.isWatchAccount }

    init {

        viewModelScope.launch {
            DashboardObject.isMultiFactor.collect { isMultiFactor ->
                syncNavigation()
            }
        }

        switchWallet()

        localStorage.marketsTabEnabledFlow.collectWith(viewModelScope) {
            marketsTabEnabled = it
            syncNavigation()
        }

        termsManager.termsAcceptedSignalFlow.collectWith(viewModelScope) {
            updateSettingsBadge()
        }

        wcSessionManager.pendingRequestCountFlow.collectWith(viewModelScope) {
            wcPendingRequestsCount = it
            updateSettingsBadge()
        }

        rateAppManager.showRateAppFlow.collectWith(viewModelScope) {
            showRateAppDialog = it
            emitState()
        }

        viewModelScope.launch {
            backupManager.allBackedUpFlowable.asFlow().collect {
                updateSettingsBadge()
            }
        }
        viewModelScope.launch {
            pinComponent.pinSetFlowable.asFlow().collect {
                updateSettingsBadge()
            }
        }
        viewModelScope.launch {
            accountManager.accountsFlowable.asFlow().collect {
                updateTransactionsTabEnabled()
                updateSettingsBadge()
            }
        }

        viewModelScope.launch {
            accountManager.activeAccountStateFlow.collect {
                if (it is ActiveAccountState.ActiveAccount) {
                    updateTransactionsTabEnabled()
                }
            }
        }

        accountManager.activeAccountStateFlow.collectWith(viewModelScope) {
            (it as? ActiveAccountState.ActiveAccount)?.let { state ->
                activeWallet = state.account
                emitState()
            }
        }

        updateSettingsBadge()
        updateTransactionsTabEnabled()
    }

    private fun switchWallet() {

        if (DashboardObject.notificationWalletAddress != null) {
            val account =
                accountManager.accounts.find {
                    it.type.evmAddress(Chain.Ethereum)?.hex.equals(
                        DashboardObject.notificationWalletAddress,
                        true
                    )
                }
            account?.let {
                onSelect(account)
                DashboardObject.notificationWalletAddress = null
            }
        }
    }

    override fun createState() = MainModule.UiState(
        selectedTabIndex = selectedTabIndex,
        deeplinkPage = deeplinkPage,
        mainNavItems = mainNavItems,
        showRateAppDialog = showRateAppDialog,
        contentHidden = contentHidden,
//        showWhatsNew = showWhatsNew,
        activeWallet = activeWallet,
        wcSupportState = wcSupportState,
        torEnabled = torEnabled
    )

    private fun isTransactionsTabEnabled(): Boolean =
        !accountManager.isAccountsEmpty && accountManager.activeAccount?.type !is AccountType.Cex


//    fun whatsNewShown() {
//        showWhatsNew = false
//        emitState()
//    }

    fun closeRateDialog() {
        showRateAppDialog = false
        emitState()
    }

    fun onSelect(account: Account) {
        accountManager.setActiveAccountId(account.id)
        activeWallet = account
        DashboardObject.updateIsDashboardOpened(true)
        emitState()
    }

    fun onResume() {
        contentHidden = pinComponent.isLocked
        emitState()
//        viewModelScope.launch {
//            if (!pinComponent.isLocked && releaseNotesManager.shouldShowChangeLog()) {
//                showWhatsNew()
//            }
//        }
    }

    fun onSelect(mainNavItem: MainNavigation) {
        if (mainNavItem != MainNavigation.Settings) {
            currentMainTab = mainNavItem
        }

//        if (mainNavItem == MainNavigation.Home){
//            DashboardObject.updateIsDashboardOpened(true)
//        }

        selectedTabIndex = items.indexOf(mainNavItem)
        syncNavigation()
    }

    private fun updateTransactionsTabEnabled() {
        transactionsEnabled = isTransactionsTabEnabled()
        syncNavigation()
    }

    fun wcSupportStateHandled() {
        wcSupportState = null
        emitState()
    }

    private fun navigationItems(): List<MainModule.NavigationViewItem> {
        return items.mapIndexed { index, mainNavItem ->
            getNavItem(mainNavItem, index == selectedTabIndex)
        }
    }

    private fun getNavItem(item: MainNavigation, selected: Boolean) = when (item) {
        MainNavigation.Home -> {
            MainModule.NavigationViewItem(
                mainNavItem = item,
                selected = selected,
                enabled = true,
            )
        }

        MainNavigation.Market -> {
            MainModule.NavigationViewItem(
                mainNavItem = item,
                selected = selected,
                enabled = !(DashboardObject.isMultiFactor.value ?: false),
            )
        }

        MainNavigation.Transactions -> {
            MainModule.NavigationViewItem(
                mainNavItem = item,
                selected = selected,
                enabled = transactionsEnabled && !(DashboardObject.isMultiFactor.value?:false),
            )
        }

        MainNavigation.Settings -> {
            MainModule.NavigationViewItem(
                mainNavItem = item,
                selected = selected,
                enabled = true,
                badge = settingsBadge
            )
        }


    }

    private fun getTabIndexToOpen(): Int {
        val tab = when {
            relaunchBySettingChange -> {
                relaunchBySettingChange = false
                MainNavigation.Settings
            }

            !marketsTabEnabled -> {
                MainNavigation.Home
            }

            else -> getLaunchTab()
        }

        return items.indexOf(tab)
    }

    private fun getLaunchTab(): MainNavigation = when (launchPage) {
        LaunchPage.Market,
        LaunchPage.Watchlist -> MainNavigation.Market

        LaunchPage.Balance -> MainNavigation.Home
        LaunchPage.Auto -> currentMainTab
    }

    private fun getNavigationDataForDeeplink(deepLink: Uri): Pair<MainNavigation, DeeplinkPage?> {
        var tab = currentMainTab
        var deeplinkPage: DeeplinkPage? = null
        val deeplinkString = deepLink.toString()
        val deeplinkScheme: String = Translator.getString(R.string.DeeplinkScheme)
        when {
            deeplinkString.startsWith("$deeplinkScheme:") -> {
                val uid = deepLink.getQueryParameter("uid")
                when {
                    deeplinkString.contains("coin-page") -> {
                        uid?.let {
                            deeplinkPage = DeeplinkPage(R.id.coinFragment, CoinFragment.Input(it))

                            stat(page = StatPage.Widget, event = StatEvent.OpenCoin(it))
                        }
                    }

                    deeplinkString.contains("nft-collection") -> {
                        val blockchainTypeUid = deepLink.getQueryParameter("blockchainTypeUid")
                        if (uid != null && blockchainTypeUid != null) {
                            deeplinkPage = DeeplinkPage(
                                R.id.nftCollectionFragment,
                                NftCollectionFragment.Input(uid, blockchainTypeUid)
                            )

                            stat(
                                page = StatPage.Widget,
                                event = StatEvent.Open(StatPage.TopNftCollections)
                            )
                        }
                    }

                    deeplinkString.contains("top-platforms") -> {
                        val title = deepLink.getQueryParameter("title")
                        if (title != null && uid != null) {
                            val platform = Platform(uid, title)
                            deeplinkPage = DeeplinkPage(R.id.marketPlatformFragment, platform)

                            stat(
                                page = StatPage.Widget,
                                event = StatEvent.Open(StatPage.TopPlatform)
                            )
                        }
                    }
                }

                tab = MainNavigation.Market
            }

            deeplinkString.startsWith("wc:") -> {
                wcSupportState = wcManager.getWalletConnectSupportState()
                if (wcSupportState == WCManager.SupportState.Supported) {
                    deeplinkPage =
                        DeeplinkPage(R.id.wcListFragment, WCListFragment.Input(deeplinkString))
                    tab = MainNavigation.Settings
                }
            }

//            deeplinkString.startsWith("tc:") -> {
//                deeplinkPage = DeeplinkPage(R.id.tcListFragment, TonConnectMainFragment.Input(deeplinkString))
//                tab = MainNavigation.Settings
//            }

            deeplinkString.startsWith("https://payfunds.money/referral") -> {
                val userId: String? = deepLink.getQueryParameter("userId")
                val referralCode: String? = deepLink.getQueryParameter("referralCode")
                if (userId != null && referralCode != null) {
                    registerApp(userId, referralCode)
                }
            }

            else -> {}
        }
        return Pair(tab, deeplinkPage)
    }

    private fun registerApp(userId: String, referralCode: String) {
        viewModelScope.launch {
            try {
                val response = networkManager.registerApp(userId, referralCode)
                if (response.success) {
                    //do nothing
                } else {
                    Log.e("MainViewModel", "registerApp api fail message: ${response.message}")
                }
            } catch (e: Exception) {
                Log.e("MainViewModel", "registerApp error: ", e)
            }
        }
    }

    private fun syncNavigation() {
        mainNavItems = navigationItems()
        if (selectedTabIndex >= mainNavItems.size) {
            selectedTabIndex = mainNavItems.size - 1
        }
        emitState()
    }
//
//    private suspend fun showWhatsNew() {
//        delay(2000)
//        showWhatsNew = true
//        emitState()
//    }

    private fun updateSettingsBadge() {
        val showDotBadge =
            !(backupManager.allBackedUp && termsManager.allTermsAccepted && pinComponent.isPinSet) || accountManager.hasNonStandardAccount

        settingsBadge = if (wcPendingRequestsCount > 0) {
            MainModule.BadgeType.BadgeNumber(wcPendingRequestsCount)
        } else if (showDotBadge) {
            MainModule.BadgeType.BadgeDot
        } else {
            null
        }
        syncNavigation()
    }

    fun deeplinkPageHandled() {
        deeplinkPage = null
        emitState()
    }

    fun handleDeepLink(uri: Uri) {
        val (tab, deeplinkPageData) = getNavigationDataForDeeplink(uri)
        deeplinkPage = deeplinkPageData
        currentMainTab = tab
        selectedTabIndex = items.indexOf(tab)
        syncNavigation()
    }

}
