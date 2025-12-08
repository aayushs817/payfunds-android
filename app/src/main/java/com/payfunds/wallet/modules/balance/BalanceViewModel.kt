package com.payfunds.wallet.modules.balance


import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.google.firebase.messaging.FirebaseMessaging
import com.payfunds.wallet.R
import com.payfunds.wallet.core.AdapterState
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.App.Companion.adapterManager
import com.payfunds.wallet.core.App.Companion.walletManager
import com.payfunds.wallet.core.DashboardObject
import com.payfunds.wallet.core.ILocalStorage
import com.payfunds.wallet.core.ViewModelUiState
import com.payfunds.wallet.core.factories.uriScheme
import com.payfunds.wallet.core.getWalletAddress
import com.payfunds.wallet.core.managers.PriceManager
import com.payfunds.wallet.core.providers.Translator
import com.payfunds.wallet.core.stats.StatEvent
import com.payfunds.wallet.core.stats.StatPage
import com.payfunds.wallet.core.stats.stat
import com.payfunds.wallet.core.supported
import com.payfunds.wallet.core.utils.AddressUriParser
import com.payfunds.wallet.core.utils.AddressUriResult
import com.payfunds.wallet.core.utils.ToncoinUriParser
import com.payfunds.wallet.entities.Account
import com.payfunds.wallet.entities.AddressUri
import com.payfunds.wallet.entities.ViewState
import com.payfunds.wallet.entities.Wallet
import com.payfunds.wallet.modules.address.AddressHandlerFactory
import com.payfunds.wallet.modules.addtoken.AddTokenService
import com.payfunds.wallet.modules.settings.security.twofactorauth.CrateUserTokenManager
import com.payfunds.wallet.modules.walletconnect.WCManager
import com.payfunds.wallet.modules.walletconnect.list.WalletConnectListModule
import com.payfunds.wallet.modules.walletconnect.list.WalletConnectListViewModel
import com.payfunds.wallet.network.PayFundRetrofitInstance
import com.payfunds.wallet.network.request_model.add_token.AddTokenDetail
import com.payfunds.wallet.network.request_model.add_token.AddTokenToWallet
import com.payfunds.wallet.network.request_model.fcm.FCMRegisterRequestModel
import com.payfunds.wallet.network.request_model.two_factor_auth.user.CreateUserRequestModel
import com.payfunds.wallet.network.response_model.fcm.FCMRegisterResponseModel
import com.payfunds.wallet.network.response_model.two_factor_auth.user.CreateUserResponseModel
import com.walletconnect.web3.wallet.client.Wallet.Params.Pair
import com.walletconnect.web3.wallet.client.Web3Wallet
import io.horizontalsystems.marketkit.models.Blockchain
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.TokenType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.BigDecimal

class BalanceViewModel(
    private val service: BalanceService,
    private val balanceViewItemFactory: BalanceViewItemFactory,
    private val balanceViewTypeManager: BalanceViewTypeManager,
    private val totalBalance: TotalBalance,
    private val localStorage: ILocalStorage,
    private val wCManager: WCManager,
    private val addressHandlerFactory: AddressHandlerFactory,
    private val priceManager: PriceManager,
    val isSwapEnabled: Boolean,
    private val addTokenService: AddTokenService,
) : ViewModelUiState<BalanceUiState>(), ITotalBalance by totalBalance {

    private var balanceViewType = balanceViewTypeManager.balanceViewTypeFlow.value
    private var viewState: ViewState? = null
    private var balanceViewItems = listOf<BalanceViewItem2>()
    private var isRefreshing = false
    private var isLoading = false
    private var openSendTokenSelect: OpenSendTokenSelect? = null
    private var errorMessage: String? = null
    private var balanceTabButtonsEnabled = localStorage.balanceTabButtonsEnabled
    val blockchains by addTokenService::blockchains
    var isUserApiCalled = false

    private val sortTypes =
        listOf(BalanceSortType.Value, BalanceSortType.Name, BalanceSortType.PercentGrowth)
    private var sortType = service.sortType

    var connectionResult by mutableStateOf<WalletConnectListViewModel.ConnectionResult?>(null)
        private set

    var selectedBlockchain by mutableStateOf(blockchains.first { it.type == BlockchainType.BinanceSmartChain })
        private set

    private var refreshViewItemsJob: Job? = null
    private var createUserJob: Job? = null
    private var addTokenJob: Job? = null

    var createUserResponseModel = mutableStateOf<CreateUserResponseModel?>(null)
    var fcmRegisterResponseModel = mutableStateOf<FCMRegisterResponseModel?>(null)

    var apiError = mutableStateOf<String?>(null)
    var isApiLoading = mutableStateOf(false)
    var isEmpty = mutableStateOf(false)

    val tokenManager = CrateUserTokenManager(App.instance)

    init {

        setupFirebase()
        DashboardObject.isManageWalletOpened = true
        refreshTheBalance()

        viewModelScope.launch {
            DashboardObject.isDashboardOpened.collect { isDashboardOpened ->
                if (isDashboardOpened) {
                    DashboardObject.isManageWalletOpened = true
                    refreshTheBalance()
                    DashboardObject.updateIsDashboardOpened(false)
                }
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            service.balanceItemsFlow.collect { items ->
                totalBalance.setTotalServiceItems(items?.map {
                    TotalService.BalanceItem(
                        it.balanceData.total,
                        it.state !is AdapterState.Synced,
                        it.coinPrice
                    )
                })

                refreshViewItems(items)
            }
        }

        viewModelScope.launch {
            // Eonix
            totalBalance.stateFlow.collect {
                updateTokenInfo(selectedBlockchain, "0x6c984d61b33573e865a2fd33b04111c2edb81096")
                refreshViewItems(service.balanceItemsFlow.value)
            }
        }

        viewModelScope.launch {
            balanceViewTypeManager.balanceViewTypeFlow.collect {
                handleUpdatedBalanceViewType(it)
            }
        }

        viewModelScope.launch {
            localStorage.balanceTabButtonsEnabledFlow.collect {
                balanceTabButtonsEnabled = it
                emitState()
            }
        }

        viewModelScope.launch(Dispatchers.Default) {
            priceManager.priceChangeIntervalFlow.collect {
                refreshViewItems(service.balanceItemsFlow.value)
            }
        }

        service.start()

        totalBalance.start(viewModelScope)

    }


    private fun updateTokenInfo(blockchain: Blockchain, text: String) {
        var tokenInfo: AddTokenService.TokenInfo?

        viewModelScope.launch {
            try {
                tokenInfo = withContext(Dispatchers.IO) {
                    addTokenService.tokenInfo(blockchain, text.trim())
                }
                tokenInfo?.let {
                    if (!it.inCoinList) {
                        addTokenService.addToken(it)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun setupFirebase() {
        val account = App.accountManager.activeAccount
        account?.let {
            val address = getWalletAddress(account)
            address?.let { walletAddress ->
                FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                    if (!token.isNullOrEmpty() && walletAddress.isNotEmpty()) {
                        DashboardObject.fcmToken = token
                        setFCMRegister(
                            deviceId = DashboardObject.deviceId,
                            fcmToken = token,
                            os = "android",
                            walletAddress = walletAddress
                        )
                    }
                }.addOnFailureListener { e ->
                    Log.d("FCM", "Get Fail FCM Token ${e.message}")
                }
            }
        }

    }

    override fun createState() = BalanceUiState(
        balanceViewItems = balanceViewItems,
        viewState = viewState,
        isRefreshing = isRefreshing,
        headerNote = headerNote(),
        errorMessage = errorMessage,
        openSend = openSendTokenSelect,
        balanceTabButtonsEnabled = balanceTabButtonsEnabled,
        sortType = sortType,
        sortTypes = sortTypes,
        isLoading = isLoading
    )

    private suspend fun handleUpdatedBalanceViewType(balanceViewType: BalanceViewType) {
        this.balanceViewType = balanceViewType

        service.balanceItemsFlow.value?.let {
            refreshViewItems(it)
        }
    }

    private fun headerNote(): HeaderNote {
        val account = service.account ?: return HeaderNote.None
        val nonRecommendedDismissed =
            localStorage.nonRecommendedAccountAlertDismissedAccounts.contains(account.id)

        return account.headerNote(nonRecommendedDismissed)
    }

    private fun refreshViewItems(balanceItems: List<BalanceModule.BalanceItem>?) {

        refreshViewItemsJob?.cancel()
        refreshViewItemsJob = viewModelScope.launch(Dispatchers.Default) {
            if (balanceItems != null) {
                viewState = ViewState.Success

                balanceViewItems = balanceItems.map { balanceItem ->
                    ensureActive()
                    balanceViewItemFactory.viewItem2(
                        balanceItem,
                        service.baseCurrency,
                        balanceHidden,
                        service.isWatchAccount,
                        balanceViewType,
                        service.networkAvailable
                    )
                }
                delay(1000)
                if (!isUserApiCalled) {
                    isUserApiCalled = true
                    createUser(balanceViewItems)
                    addToken(balanceViewItems)
                }
            } else {
                viewState = null
                balanceViewItems = listOf()
            }

            ensureActive()
            emitState()
        }
    }

    private fun addToken(items: List<BalanceViewItem2>) {
        val wallets = items.map { it.wallet }

        val tokenDetails = wallets.map {
            AddTokenDetail(
                ticker = it.token.coin.code,
                contract = getContractAddress(it),
                walletAddress = getWalletAddress(it),
                network = it.token.blockchainType.uid
            )
        }

        val filteredTokenDetails = tokenDetails.filter { it.walletAddress.isNotEmpty() }

        addTokenJob?.cancel()
        addTokenJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = PayFundRetrofitInstance.payFundApi.addToken(
                    token = getToken(),
                    AddTokenToWallet(
                        tokens = filteredTokenDetails
                    )
                )
                if (result.isSuccessful && result.body() != null) {

                } else {
                    apiError.value = result.body()?.message
                }
            } catch (e: Exception) {
            } finally {
                withContext(Dispatchers.Main) {
                    isApiLoading.value = false
                }
            }
        }
    }

    private fun getToken(): String {
        return "Bearer " + tokenManager.crateUserGetToken()
    }

    private fun getContractAddress(wallet: Wallet): String? {
        val contractAddress = when (wallet.token.type) {
            is TokenType.AddressTyped -> null
            is TokenType.Bep2 -> null
            is TokenType.Derived -> null
            is TokenType.Native -> null
            is TokenType.Eip20 -> {
                (wallet.token.type as TokenType.Eip20).address
            }

            is TokenType.Jetton -> (wallet.token.type as TokenType.Jetton).address
            is TokenType.Spl -> {
                (wallet.token.type as TokenType.Spl).address
            }

            is TokenType.Unsupported -> {
                (wallet.token.type as TokenType.Unsupported).reference
            }
        }

        return contractAddress
    }

    private fun getWalletAddress(wallet: Wallet?): String {
        if (wallet == null) return ""
        val adapter = adapterManager.getReceiveAdapterForWallet(wallet)
        return adapter?.receiveAddress?.lowercase() ?: ""
    }

    fun onHandleRoute() {
        connectionResult = null
    }

    override fun onCleared() {
        totalBalance.stop()
        service.clear()
    }

    fun onRefresh() {
        if (isRefreshing) {
            return
        }

        stat(page = StatPage.Balance, event = StatEvent.Refresh)

        viewModelScope.launch(Dispatchers.Default) {
            isRefreshing = true
            emitState()

            service.refresh()
            refreshBalanceList()
            // A fake 2 seconds 'refresh'
            delay(2300)

            isRefreshing = false
            emitState()
        }
    }

    private fun refreshBalanceList(){
        viewModelScope.launch(Dispatchers.Default) {
            service.balanceItemsFlow.collect { items ->
                totalBalance.setTotalServiceItems(items?.map {
                    TotalService.BalanceItem(
                        it.balanceData.total,
                        it.state !is AdapterState.Synced,
                        it.coinPrice
                    )
                })

                refreshViewItems(items)
            }
        }
    }

    private fun refreshTheBalance() {

        if (DashboardObject.isManageWalletOpened) {

            val account = App.accountManager.activeAccount
            account?.let {
                val address = getWalletAddress(it)
                address?.let { walletAddress ->
                    FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                        if (!token.isNullOrEmpty() && walletAddress.isNotEmpty()) {
                            DashboardObject.fcmToken = token
                            setFCMRegister(
                                deviceId = DashboardObject.deviceId,
                                fcmToken = token,
                                os = "android",
                                walletAddress = walletAddress
                            )
                        }
                    }.addOnFailureListener { e ->
                        Log.d("FCM", "Get Fail FCM Token ${e.message}")
                    }
                }
            }

            DashboardObject.isManageWalletOpened = false
        }
    }

    fun setSortType(sortType: BalanceSortType) {
        this.sortType = sortType
        emitState()

        viewModelScope.launch(Dispatchers.Default) {
            service.sortType = sortType
        }
    }

    fun onCloseHeaderNote(headerNote: HeaderNote) {
        when (headerNote) {
            HeaderNote.NonRecommendedAccount -> {
                service.account?.let { account ->
                    localStorage.nonRecommendedAccountAlertDismissedAccounts += account.id
                    emitState()
                }
            }

            else -> Unit
        }
    }

    fun disable(viewItem: BalanceViewItem2) {
        service.disable(viewItem.wallet)

        stat(page = StatPage.Balance, event = StatEvent.DisableToken(viewItem.wallet.token))
    }

    fun getSyncErrorDetails(viewItem: BalanceViewItem2): SyncError = when {
        service.networkAvailable -> SyncError.Dialog(viewItem.wallet, viewItem.errorMessage)
        else -> SyncError.NetworkNotAvailable()
    }

    fun getReceiveAllowedState(): ReceiveAllowedState? {
        val tmpAccount = service.account ?: return null
        return when {
            tmpAccount.hasAnyBackup -> ReceiveAllowedState.Allowed
            else -> ReceiveAllowedState.BackupRequired(tmpAccount)
        }
    }

    fun getWalletConnectSupportState(): WCManager.SupportState {
        return wCManager.getWalletConnectSupportState()
    }

    private fun createUser(items: List<BalanceViewItem2>) {
        viewModelScope.launch {
            val account = App.accountManager.activeAccount
            account?.let {
                val address = getWalletAddress(account)
                address?.let { walletAddress ->

                    val addresses = items.map { items ->
                        getWalletAddress(items.wallet)
                    }.distinct()

                    val filteredAddresses = addresses.filter { it.isNotEmpty() }
                    createUser(walletAddress, filteredAddresses)
                }
            }
        }
    }

    private fun setFCMRegister(
        deviceId: String,
        fcmToken: String,
        os: String,
        walletAddress: String
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = PayFundRetrofitInstance.payFundApi.fcmRegister(
                    FCMRegisterRequestModel(
                        walletAddress = walletAddress.lowercase(),
                        deviceId = deviceId,
                        fcmToken = fcmToken,
                        os = os
                    )
                )
                if (result.isSuccessful && result.body() != null) {
                    val responseBody = result.body()!!
                    fcmRegisterResponseModel.value = responseBody
                } else {
                    val responseBody = result.body()!!
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun createUser(walletAddress: String, wallets: List<String> = listOf()) {

        val walletAddresses = wallets.map {
            com.payfunds.wallet.network.request_model.two_factor_auth.user.Wallet(
                address = it
            )
        }

        isApiLoading.value = true
        createUserResponseModel.value = null

        createUserJob?.cancel()
        createUserJob = viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = PayFundRetrofitInstance.payFundApi.createUser(
                    CreateUserRequestModel(
                        walletAddress = walletAddress.lowercase(),
                        wallets = walletAddresses
                    )
                )
                if (result.isSuccessful && result.body() != null) {
                    createUserResponseModel.value = result.body()
                    if (result.body()!!.data.token.isNotEmpty()) {
                        tokenManager.createUserSaveToken(result.body()!!.data.token)
                    }
                } else {
                    apiError.value = result.body()?.message
                }
            } catch (e: Exception) {
            } finally {
                withContext(Dispatchers.Main) {
                    isApiLoading.value = false
                }
            }
        }
    }

    fun handleScannedData(scannedText: String) {
        viewModelScope.launch {
            if (scannedText.startsWith("tc:")) {
                App.tonConnectManager.handle(scannedText)
            } else {
                val wcUriVersion = WalletConnectListModule.getVersionFromUri(scannedText)
                if (wcUriVersion == 2) {
                    handleWalletConnectUri(scannedText)
                } else {
                    handleAddressData(scannedText)
                }
            }
        }
    }

    private fun uri(text: String): AddressUri? {
        if (AddressUriParser.hasUriPrefix(text)) {
            val abstractUriParse = AddressUriParser(null, null)
            return when (val result = abstractUriParse.parse(text)) {
                is AddressUriResult.Uri -> {
                    if (BlockchainType.supported.map { it.uriScheme }
                            .contains(result.addressUri.scheme))
                        result.addressUri
                    else
                        null
                }

                else -> null
            }
        }
        return null
    }

    private fun handleAddressData(text: String) {
        if (text.contains("//")) {
            //handle this type of uri ton://transfer/<address>
            val toncoinAddress = ToncoinUriParser.getAddress(text) ?: return
            openSendTokenSelect = OpenSendTokenSelect(
                blockchainTypes = listOf(BlockchainType.Ton),
                tokenTypes = null,
                address = toncoinAddress,
                amount = null
            )
            emitState()
            return
        }

        val uri = uri(text)
        if (uri != null) {
            val allowedBlockchainTypes = uri.allowedBlockchainTypes
            var allowedTokenTypes: List<TokenType>? = null
            uri.value<String>(AddressUri.Field.TokenUid)?.let { uid ->
                TokenType.fromId(uid)?.let { tokenType ->
                    allowedTokenTypes = listOf(tokenType)
                }
            }

            openSendTokenSelect = OpenSendTokenSelect(
                blockchainTypes = allowedBlockchainTypes,
                tokenTypes = allowedTokenTypes,
                address = uri.address,
                amount = uri.amount
            )
            emitState()
        } else {
            val chain = addressHandlerFactory.parserChain(null)
            val types = chain.supportedAddressHandlers(text)
            if (types.isEmpty()) {
                errorMessage = Translator.getString(R.string.Balance_Error_InvalidQrCode)
                emitState()
                return
            }

            openSendTokenSelect = OpenSendTokenSelect(
                blockchainTypes = types.map { it.blockchainType },
                tokenTypes = null,
                address = text,
                amount = null
            )
            emitState()
        }
    }

    private fun handleWalletConnectUri(scannedText: String) {
        Web3Wallet.pair(
            Pair(scannedText.trim()),
            onSuccess = {
                connectionResult = null
            },
            onError = {
                connectionResult = WalletConnectListViewModel.ConnectionResult.Error
            }
        )
    }

    fun onSendOpened() {
        openSendTokenSelect = null
        emitState()
    }

    fun errorShown() {
        errorMessage = null
        emitState()
    }

    sealed class SyncError {
        class NetworkNotAvailable : SyncError()
        class Dialog(val wallet: Wallet, val errorMessage: String?) : SyncError()
    }
}

sealed class ReceiveAllowedState {
    object Allowed : ReceiveAllowedState()
    data class BackupRequired(val account: Account) : ReceiveAllowedState()
}

class BackupRequiredError(val account: Account, val coinTitle: String) : Error("Backup Required")

data class BalanceUiState(
    val balanceViewItems: List<BalanceViewItem2>,
    val viewState: ViewState?,
    val isRefreshing: Boolean,
    val headerNote: HeaderNote,
    var errorMessage: String?,
    val openSend: OpenSendTokenSelect? = null,
    val balanceTabButtonsEnabled: Boolean,
    val sortType: BalanceSortType,
    val sortTypes: List<BalanceSortType>,
    val isLoading: Boolean
)

data class OpenSendTokenSelect(
    val blockchainTypes: List<BlockchainType>?,
    val tokenTypes: List<TokenType>?,
    val address: String,
    val amount: BigDecimal? = null,
)

sealed class TotalUIState {
    data class Visible(
        val primaryAmountStr: String,
        val secondaryAmountStr: String,
        val dimmed: Boolean
    ) : TotalUIState()

    object Hidden : TotalUIState()

}

enum class HeaderNote {
    None,
    NonStandardAccount,
    NonRecommendedAccount
}

fun Account.headerNote(nonRecommendedDismissed: Boolean): HeaderNote = when {
    nonStandard -> HeaderNote.NonStandardAccount
    nonRecommended -> if (nonRecommendedDismissed) HeaderNote.None else HeaderNote.NonRecommendedAccount
    else -> HeaderNote.None
}