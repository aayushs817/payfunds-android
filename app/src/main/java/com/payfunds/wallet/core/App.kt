package com.payfunds.wallet.core

import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import androidx.preference.PreferenceManager
import coil.ImageLoader
import coil.ImageLoaderFactory
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import com.walletconnect.android.Core
import com.walletconnect.android.CoreClient
import com.walletconnect.android.relay.ConnectionType
import com.walletconnect.web3.wallet.client.Wallet
import com.walletconnect.web3.wallet.client.Web3Wallet
import io.horizontalsystems.ethereumkit.core.EthereumKit
import io.horizontalsystems.hdwalletkit.Mnemonic
import com.payfunds.wallet.core.factories.AccountFactory
import com.payfunds.wallet.core.factories.AdapterFactory
import com.payfunds.wallet.core.factories.EvmAccountManagerFactory
import com.payfunds.wallet.core.managers.AccountCleaner
import com.payfunds.wallet.core.managers.AccountManager
import com.payfunds.wallet.core.managers.AdapterManager
import com.payfunds.wallet.core.managers.AppVersionManager
import com.payfunds.wallet.core.managers.BackupManager
import com.payfunds.wallet.core.managers.BalanceHiddenManager
import com.payfunds.wallet.core.managers.BaseTokenManager
import com.payfunds.wallet.core.managers.BinanceKitManager
import com.payfunds.wallet.core.managers.BtcBlockchainManager
import com.payfunds.wallet.core.managers.CexAssetManager
import com.payfunds.wallet.core.managers.CoinManager
import com.payfunds.wallet.core.managers.ConnectivityManager
import com.payfunds.wallet.core.managers.CurrencyManager
import com.payfunds.wallet.core.managers.EvmBlockchainManager
import com.payfunds.wallet.core.managers.EvmLabelManager
import com.payfunds.wallet.core.managers.EvmSyncSourceManager
import com.payfunds.wallet.core.managers.KeyStoreCleaner
import com.payfunds.wallet.core.managers.LanguageManager
import com.payfunds.wallet.core.managers.LocalStorageManager
import com.payfunds.wallet.core.managers.MarketFavoritesManager
import com.payfunds.wallet.core.managers.MarketKitWrapper
import com.payfunds.wallet.core.managers.NetworkManager
import com.payfunds.wallet.core.managers.NftAdapterManager
import com.payfunds.wallet.core.managers.NftMetadataManager
import com.payfunds.wallet.core.managers.NftMetadataSyncer
import com.payfunds.wallet.core.managers.NumberFormatter
import com.payfunds.wallet.core.managers.PriceManager
import com.payfunds.wallet.core.managers.RateAppManager
import com.payfunds.wallet.core.managers.ReleaseNotesManager
import com.payfunds.wallet.core.managers.RestoreSettingsManager
import com.payfunds.wallet.core.managers.SolanaKitManager
import com.payfunds.wallet.core.managers.SolanaRpcSourceManager
import com.payfunds.wallet.core.managers.SolanaWalletManager
import com.payfunds.wallet.core.managers.SpamManager
import com.payfunds.wallet.core.managers.SubscriptionManager
import com.payfunds.wallet.core.managers.SystemInfoManager
import com.payfunds.wallet.core.managers.TermsManager
import com.payfunds.wallet.core.managers.TokenAutoEnableManager
import com.payfunds.wallet.core.managers.TonAccountManager
import com.payfunds.wallet.core.managers.TonConnectManager
import com.payfunds.wallet.core.managers.TonKitManager
import com.payfunds.wallet.core.managers.TorManager
import com.payfunds.wallet.core.managers.TransactionAdapterManager
import com.payfunds.wallet.core.managers.TronAccountManager
import com.payfunds.wallet.core.managers.TronKitManager
import com.payfunds.wallet.core.managers.UserManager
import com.payfunds.wallet.core.managers.WalletActivator
import com.payfunds.wallet.core.managers.WalletManager
import com.payfunds.wallet.core.managers.WalletStorage
import com.payfunds.wallet.core.managers.WordsManager
import com.payfunds.wallet.core.managers.ZcashBirthdayProvider
import com.payfunds.wallet.core.providers.AppConfigProvider
import com.payfunds.wallet.core.providers.CexProviderManager
import com.payfunds.wallet.core.providers.EvmLabelProvider
import com.payfunds.wallet.core.providers.FeeRateProvider
import com.payfunds.wallet.core.providers.FeeTokenProvider
import com.payfunds.wallet.core.stats.StatsManager
import com.payfunds.wallet.core.storage.AccountsStorage
import com.payfunds.wallet.core.storage.AppDatabase
import com.payfunds.wallet.core.storage.BlockchainSettingsStorage
import com.payfunds.wallet.core.storage.EnabledWalletsStorage
import com.payfunds.wallet.core.storage.EvmSyncSourceStorage
import com.payfunds.wallet.core.storage.NftStorage
import com.payfunds.wallet.core.storage.RestoreSettingsStorage
import com.payfunds.wallet.modules.backuplocal.fullbackup.BackupProvider
import com.payfunds.wallet.modules.balance.BalanceViewTypeManager
import com.payfunds.wallet.modules.chart.ChartIndicatorManager
import com.payfunds.wallet.modules.contacts.ContactsRepository
import com.payfunds.wallet.modules.market.favorites.MarketFavoritesMenuService
import com.payfunds.wallet.modules.market.topnftcollections.TopNftCollectionsRepository
import com.payfunds.wallet.modules.market.topnftcollections.TopNftCollectionsViewItemFactory
import com.payfunds.wallet.modules.market.topplatforms.TopPlatformsRepository
import com.payfunds.wallet.modules.pin.PinComponent
import com.payfunds.wallet.modules.pin.core.PinDbStorage
import com.payfunds.wallet.modules.profeatures.ProFeaturesAuthorizationManager
import com.payfunds.wallet.modules.profeatures.storage.ProFeaturesStorage
import com.payfunds.wallet.modules.settings.appearance.AppIconService
import com.payfunds.wallet.modules.settings.appearance.LaunchScreenService
import com.payfunds.wallet.modules.theme.ThemeService
import com.payfunds.wallet.modules.theme.ThemeType
import com.payfunds.wallet.modules.walletconnect.WCManager
import com.payfunds.wallet.modules.walletconnect.WCSessionManager
import com.payfunds.wallet.modules.walletconnect.WCWalletRequestHandler
import com.payfunds.wallet.modules.walletconnect.storage.WCSessionStorage
import com.payfunds.wallet.network.response_model.two_factor_auth.user.CreateUserResponseModel
import com.payfunds.wallet.widgets.MarketWidgetManager
import com.payfunds.wallet.widgets.MarketWidgetRepository
import com.payfunds.wallet.widgets.MarketWidgetWorker
import io.payfunds.core.BackgroundManager
import io.payfunds.core.CoreApp
import io.payfunds.core.ICoreApp
import io.payfunds.core.security.EncryptionManager
import io.payfunds.core.security.KeyStoreManager
import io.reactivex.plugins.RxJavaPlugins
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import zendesk.core.AnonymousIdentity
import zendesk.core.Zendesk
import zendesk.support.Support
import java.security.MessageDigest
import java.util.logging.Level
import java.util.logging.Logger
import androidx.work.Configuration as WorkConfiguration

class App : CoreApp(), WorkConfiguration.Provider, ImageLoaderFactory {

    companion object : ICoreApp by CoreApp {
        lateinit var createUserResponse: CreateUserResponseModel
        lateinit var preferences: SharedPreferences
        lateinit var feeRateProvider: FeeRateProvider
        lateinit var localStorage: ILocalStorage
        lateinit var marketStorage: IMarketStorage
        lateinit var torKitManager: ITorManager
        lateinit var restoreSettingsStorage: IRestoreSettingsStorage
        lateinit var currencyManager: CurrencyManager
        lateinit var languageManager: LanguageManager

        lateinit var blockchainSettingsStorage: BlockchainSettingsStorage
        lateinit var evmSyncSourceStorage: EvmSyncSourceStorage
        lateinit var btcBlockchainManager: BtcBlockchainManager
        lateinit var wordsManager: WordsManager
        lateinit var networkManager: INetworkManager
        lateinit var appConfigProvider: AppConfigProvider
        lateinit var adapterManager: IAdapterManager
        lateinit var transactionAdapterManager: TransactionAdapterManager
        lateinit var walletManager: IWalletManager
        lateinit var walletActivator: WalletActivator
        lateinit var tokenAutoEnableManager: TokenAutoEnableManager
        lateinit var walletStorage: IWalletStorage
        lateinit var accountManager: IAccountManager
        lateinit var userManager: UserManager
        lateinit var accountFactory: IAccountFactory
        lateinit var backupManager: IBackupManager
        lateinit var proFeatureAuthorizationManager: ProFeaturesAuthorizationManager
        lateinit var zcashBirthdayProvider: ZcashBirthdayProvider

        lateinit var connectivityManager: ConnectivityManager
        lateinit var appDatabase: AppDatabase
        lateinit var accountsStorage: IAccountsStorage
        lateinit var enabledWalletsStorage: IEnabledWalletStorage
        lateinit var binanceKitManager: BinanceKitManager
        lateinit var solanaKitManager: SolanaKitManager
        lateinit var tronKitManager: TronKitManager
        lateinit var tonKitManager: TonKitManager
        lateinit var numberFormatter: IAppNumberFormatter
        lateinit var feeCoinProvider: FeeTokenProvider
        lateinit var accountCleaner: IAccountCleaner
        lateinit var rateAppManager: IRateAppManager
        lateinit var coinManager: ICoinManager
        lateinit var wcSessionManager: WCSessionManager
        lateinit var wcManager: WCManager
        lateinit var wcWalletRequestHandler: WCWalletRequestHandler
        lateinit var termsManager: ITermsManager
        lateinit var marketFavoritesManager: MarketFavoritesManager
        lateinit var marketKit: MarketKitWrapper
        lateinit var priceManager: PriceManager
        lateinit var releaseNotesManager: ReleaseNotesManager
        lateinit var restoreSettingsManager: RestoreSettingsManager
        lateinit var evmSyncSourceManager: EvmSyncSourceManager
        lateinit var evmBlockchainManager: EvmBlockchainManager
        lateinit var solanaRpcSourceManager: SolanaRpcSourceManager
        lateinit var nftMetadataManager: NftMetadataManager
        lateinit var nftAdapterManager: NftAdapterManager
        lateinit var nftMetadataSyncer: NftMetadataSyncer
        lateinit var evmLabelManager: EvmLabelManager
        lateinit var baseTokenManager: BaseTokenManager
        lateinit var balanceViewTypeManager: BalanceViewTypeManager
        lateinit var balanceHiddenManager: BalanceHiddenManager
        lateinit var marketWidgetManager: MarketWidgetManager
        lateinit var marketWidgetRepository: MarketWidgetRepository
        lateinit var contactsRepository: ContactsRepository
        lateinit var subscriptionManager: SubscriptionManager
        lateinit var cexProviderManager: CexProviderManager
        lateinit var cexAssetManager: CexAssetManager
        lateinit var chartIndicatorManager: ChartIndicatorManager
        lateinit var backupProvider: BackupProvider
        lateinit var spamManager: SpamManager
        lateinit var statsManager: StatsManager
        lateinit var tonConnectManager: TonConnectManager
    }

    private val coroutineScope = CoroutineScope(Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()

        if (!com.payfunds.wallet.BuildConfig.DEBUG) {
            //Disable logging for lower levels in Release build
            Logger.getLogger("").level = Level.SEVERE
        }

        RxJavaPlugins.setErrorHandler { e: Throwable? ->
            Log.w("RxJava ErrorHandler", e)
        }

        instance = this
        preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext)

        LocalStorageManager(preferences).apply {
            localStorage = this
            pinSettingsStorage = this
            lockoutStorage = this
            thirdKeyboardStorage = this
            marketStorage = this
        }

        val appConfig = AppConfigProvider(localStorage)
        appConfigProvider = appConfig

        torKitManager = TorManager(instance, localStorage)
        subscriptionManager = SubscriptionManager()

        marketKit = MarketKitWrapper(
            context = this,
            hsApiBaseUrl = appConfig.marketApiBaseUrl,
            hsApiKey = appConfig.marketApiKey,
            subscriptionManager = subscriptionManager
        )

        priceManager = PriceManager(localStorage)

        feeRateProvider = FeeRateProvider(appConfigProvider)
        backgroundManager = BackgroundManager(this)

        appDatabase = AppDatabase.getInstance(this)

        blockchainSettingsStorage = BlockchainSettingsStorage(appDatabase)
        evmSyncSourceStorage = EvmSyncSourceStorage(appDatabase)
        evmSyncSourceManager =
            EvmSyncSourceManager(appConfigProvider, blockchainSettingsStorage, evmSyncSourceStorage)

        btcBlockchainManager = BtcBlockchainManager(blockchainSettingsStorage, marketKit)

        binanceKitManager = BinanceKitManager()

        accountsStorage = AccountsStorage(appDatabase)
        restoreSettingsStorage = RestoreSettingsStorage(appDatabase)

        AppLog.logsDao = appDatabase.logsDao()

        accountCleaner = AccountCleaner()
        accountManager = AccountManager(accountsStorage, accountCleaner)
        userManager = UserManager(accountManager)

        val proFeaturesStorage = ProFeaturesStorage(appDatabase)
        proFeatureAuthorizationManager =
            ProFeaturesAuthorizationManager(proFeaturesStorage, accountManager, appConfigProvider)

        enabledWalletsStorage = EnabledWalletsStorage(appDatabase)
        walletStorage = WalletStorage(marketKit, enabledWalletsStorage)

        walletManager = WalletManager(accountManager, walletStorage)
        coinManager = CoinManager(marketKit, walletManager)

        solanaRpcSourceManager = SolanaRpcSourceManager(blockchainSettingsStorage, marketKit)
        val solanaWalletManager = SolanaWalletManager(walletManager, accountManager, marketKit)
        solanaKitManager = SolanaKitManager(
            appConfigProvider,
            solanaRpcSourceManager,
            solanaWalletManager,
            backgroundManager
        )

        tronKitManager = TronKitManager(appConfigProvider, backgroundManager)
        tonKitManager = TonKitManager(backgroundManager)

        wordsManager = WordsManager(Mnemonic())
        networkManager = NetworkManager()
        accountFactory = AccountFactory(accountManager, userManager)
        backupManager = BackupManager(accountManager)


        KeyStoreManager(
            keyAlias = "MASTER_KEY",
            keyStoreCleaner = KeyStoreCleaner(localStorage, accountManager, walletManager),
            logger = AppLogger("key-store")
        ).apply {
            keyStoreManager = this
            keyProvider = this
        }

        encryptionManager = EncryptionManager(keyProvider)

        walletActivator = WalletActivator(walletManager, marketKit)
        tokenAutoEnableManager = TokenAutoEnableManager(appDatabase.tokenAutoEnabledBlockchainDao())

        val evmAccountManagerFactory = EvmAccountManagerFactory(
            accountManager,
            walletManager,
            marketKit,
            tokenAutoEnableManager
        )
        evmBlockchainManager = EvmBlockchainManager(
            backgroundManager,
            evmSyncSourceManager,
            marketKit,
            evmAccountManagerFactory,
        )

        val tronAccountManager = TronAccountManager(
            accountManager,
            walletManager,
            marketKit,
            tronKitManager,
            tokenAutoEnableManager
        )
        tronAccountManager.start()

        val tonAccountManager =
            TonAccountManager(accountManager, walletManager, tonKitManager, tokenAutoEnableManager)
        tonAccountManager.start()

        systemInfoManager = SystemInfoManager(appConfigProvider)

        languageManager = LanguageManager()
        currencyManager = CurrencyManager(localStorage, appConfigProvider)
        numberFormatter = NumberFormatter(languageManager)

        connectivityManager = ConnectivityManager(backgroundManager)

        zcashBirthdayProvider = ZcashBirthdayProvider(this)
        restoreSettingsManager =
            RestoreSettingsManager(restoreSettingsStorage, zcashBirthdayProvider)

        evmLabelManager = EvmLabelManager(
            EvmLabelProvider(),
            appDatabase.evmAddressLabelDao(),
            appDatabase.evmMethodLabelDao(),
            appDatabase.syncerStateDao()
        )

        val adapterFactory = AdapterFactory(
            context = instance,
            btcBlockchainManager = btcBlockchainManager,
            evmBlockchainManager = evmBlockchainManager,
            evmSyncSourceManager = evmSyncSourceManager,
            binanceKitManager = binanceKitManager,
            solanaKitManager = solanaKitManager,
            tronKitManager = tronKitManager,
            tonKitManager = tonKitManager,
            backgroundManager = backgroundManager,
            restoreSettingsManager = restoreSettingsManager,
            coinManager = coinManager,
            evmLabelManager = evmLabelManager,
            localStorage = localStorage
        )
        adapterManager = AdapterManager(
            walletManager,
            adapterFactory,
            btcBlockchainManager,
            evmBlockchainManager,
            binanceKitManager,
            solanaKitManager,
            tronKitManager,
            tonKitManager,
        )
        transactionAdapterManager = TransactionAdapterManager(adapterManager, adapterFactory)

        feeCoinProvider = FeeTokenProvider(marketKit)

        pinComponent = PinComponent(
            pinSettingsStorage = pinSettingsStorage,
            userManager = userManager,
            pinDbStorage = PinDbStorage(appDatabase.pinDao()),
            backgroundManager = backgroundManager
        )

        statsManager = StatsManager(
            appDatabase.statsDao(),
            localStorage,
            marketKit,
            appConfigProvider,
            backgroundManager
        )

        rateAppManager = RateAppManager(walletManager, adapterManager, localStorage)

        wcManager = WCManager(accountManager)
        wcWalletRequestHandler = WCWalletRequestHandler(evmBlockchainManager)

        termsManager = TermsManager(localStorage)

        marketWidgetManager = MarketWidgetManager()
        marketFavoritesManager =
            MarketFavoritesManager(appDatabase, localStorage, marketWidgetManager)

        marketWidgetRepository = MarketWidgetRepository(
            marketKit,
            marketFavoritesManager,
            MarketFavoritesMenuService(localStorage, marketWidgetManager),
            TopNftCollectionsRepository(marketKit),
            TopNftCollectionsViewItemFactory(numberFormatter),
            TopPlatformsRepository(marketKit),
            currencyManager
        )

        releaseNotesManager =
            ReleaseNotesManager(systemInfoManager, localStorage, appConfigProvider)

        setAppTheme()

        val nftStorage = NftStorage(appDatabase.nftDao(), marketKit)
        nftMetadataManager = NftMetadataManager(marketKit, appConfigProvider, nftStorage)
        nftAdapterManager = NftAdapterManager(walletManager, evmBlockchainManager)
        nftMetadataSyncer = NftMetadataSyncer(nftAdapterManager, nftMetadataManager, nftStorage)

        initializeWalletConnectV2(appConfig)

        wcSessionManager = WCSessionManager(accountManager, WCSessionStorage(appDatabase))

        baseTokenManager = BaseTokenManager(coinManager, localStorage)
        balanceViewTypeManager = BalanceViewTypeManager(localStorage)
        balanceHiddenManager = BalanceHiddenManager(localStorage, backgroundManager)

        contactsRepository = ContactsRepository(marketKit)
        cexProviderManager = CexProviderManager(accountManager)
        cexAssetManager = CexAssetManager(marketKit, appDatabase.cexAssetsDao())
        chartIndicatorManager =
            ChartIndicatorManager(appDatabase.chartIndicatorSettingsDao(), localStorage)

        backupProvider = BackupProvider(
            localStorage = localStorage,
            languageManager = languageManager,
            walletStorage = enabledWalletsStorage,
            settingsManager = restoreSettingsManager,
            accountManager = accountManager,
            accountFactory = accountFactory,
            walletManager = walletManager,
            restoreSettingsManager = restoreSettingsManager,
            blockchainSettingsStorage = blockchainSettingsStorage,
            evmBlockchainManager = evmBlockchainManager,
            marketFavoritesManager = marketFavoritesManager,
            balanceViewTypeManager = balanceViewTypeManager,
            appIconService = AppIconService(localStorage),
            themeService = ThemeService(localStorage),
            chartIndicatorManager = chartIndicatorManager,
            chartIndicatorSettingsDao = appDatabase.chartIndicatorSettingsDao(),
            balanceHiddenManager = balanceHiddenManager,
            baseTokenManager = baseTokenManager,
            launchScreenService = LaunchScreenService(localStorage),
            currencyManager = currencyManager,
            btcBlockchainManager = btcBlockchainManager,
            evmSyncSourceManager = evmSyncSourceManager,
            evmSyncSourceStorage = evmSyncSourceStorage,
            solanaRpcSourceManager = solanaRpcSourceManager,
            contactsRepository = contactsRepository
        )

        spamManager = SpamManager(localStorage)

        tonConnectManager = TonConnectManager(this, adapterFactory)
        tonConnectManager.start()

        //Zendesk
        Zendesk.INSTANCE.init(applicationContext, appConfigProvider.zendeskUrl,
            appConfigProvider.zendeskApplicationId,
            appConfigProvider.zendeskClientId)
        Zendesk.INSTANCE.setIdentity(AnonymousIdentity())
        Support.INSTANCE.init(Zendesk.INSTANCE)


        startTasks()
    }

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .crossfade(true)
            .components {
                add(SvgDecoder.Factory())
                if (Build.VERSION.SDK_INT >= 28) {
                    add(ImageDecoderDecoder.Factory())
                } else {
                    add(GifDecoder.Factory())
                }
            }
            .build()
    }

    private fun initializeWalletConnectV2(appConfig: AppConfigProvider) {
        try {
            val projectId = appConfig.walletConnectProjectId
            val serverUrl = "wss://${appConfig.walletConnectUrl}?projectId=$projectId"
            val connectionType = ConnectionType.AUTOMATIC
            val appMetaData = Core.Model.AppMetaData(
                name = appConfig.walletConnectAppMetaDataName,
                description = "",
                url = appConfig.walletConnectAppMetaDataUrl,
                icons = listOf(appConfig.walletConnectAppMetaDataIcon),
                redirect = null,
            )

            CoreClient.initialize(
                metaData = appMetaData,
                relayServerUrl = serverUrl,
                connectionType = connectionType,
                application = this,
                onError = { error ->
                    Log.w("WalletConnect", "CoreClient initialization error", error.throwable)
                },
            )
            Web3Wallet.initialize(Wallet.Params.Init(core = CoreClient)) { error ->
                Log.e("WalletConnect", "Web3Wallet initialization error", error.throwable)
            }
        } catch (e: UnsatisfiedLinkError) {
            // Handle SQLCipher native library loading errors
            Log.e("WalletConnect", "Failed to initialize WalletConnect due to native library error. This may be due to missing SQLCipher native libraries for this device architecture.", e)
            // Continue app initialization without WalletConnect functionality
        } catch (e: Exception) {
            // Handle any other initialization errors
            Log.e("WalletConnect", "Failed to initialize WalletConnect", e)
            // Continue app initialization without WalletConnect functionality
        }
    }

    private fun setAppTheme() {
        val nightMode = when (localStorage.currentTheme) {
            ThemeType.Light -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeType.Dark -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeType.System -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }

        if (AppCompatDelegate.getDefaultNightMode() != nightMode) {
            AppCompatDelegate.setDefaultNightMode(nightMode)
        }
    }

    override val workManagerConfiguration: androidx.work.Configuration
        get() = if (com.payfunds.wallet.BuildConfig.DEBUG) {
            WorkConfiguration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .build()
        } else {
            WorkConfiguration.Builder()
                .setMinimumLoggingLevel(Log.ERROR)
                .build()
        }

    override fun localizedContext(): Context {
        return localeAwareContext(this)
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(localeAwareContext(base))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        localeAwareContext(this)
    }

    override val isSwapEnabled = true

    override fun getApplicationSignatures() = try {
        val signatureList = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val signingInfo = packageManager.getPackageInfo(
                packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            ).signingInfo

            when {
                signingInfo!!.hasMultipleSigners() -> signingInfo.apkContentsSigners // Send all with apkContentsSigners
                else -> signingInfo.signingCertificateHistory // Send one with signingCertificateHistory
            }
        } else {
            packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES).signatures
        }

        signatureList?.map {
            val digest = MessageDigest.getInstance("SHA")
            digest.update(it.toByteArray())
            digest.digest()
        }
    } catch (e: Exception) {
        // Handle error
        emptyList()
    }

    private fun startTasks() {
        coroutineScope.launch {
            EthereumKit.init()
            adapterManager.startAdapterManager()
            marketKit.sync()
            rateAppManager.onAppLaunch()
            nftMetadataSyncer.start()
            pinComponent.initDefaultPinLevel()
            accountManager.clearAccounts()
            wcSessionManager.start()

            AppVersionManager(systemInfoManager, localStorage).apply { storeAppVersion() }

            if (MarketWidgetWorker.hasEnabledWidgets(instance)) {
                MarketWidgetWorker.enqueueWork(instance)
            } else {
                MarketWidgetWorker.cancel(instance)
            }

            evmLabelManager.sync()
            contactsRepository.initialize()
        }
    }
}
