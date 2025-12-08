package com.payfunds.wallet.modules.manageaccounts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.DashboardObject
import com.payfunds.wallet.core.IAccountManager
import com.payfunds.wallet.core.getWalletAddress
import com.payfunds.wallet.core.managers.ActiveAccountState
import com.payfunds.wallet.entities.Account
import com.payfunds.wallet.modules.manageaccounts.ManageAccountsModule.AccountViewItem
import com.payfunds.wallet.modules.settings.security.twofactorauth.CrateUserTokenManager
import com.payfunds.wallet.network.PayFundRetrofitInstance
import com.payfunds.wallet.network.request_model.check_wallet_address.TwoFactorAuthCheckWalletAddressRequestModel
import com.payfunds.wallet.network.request_model.two_factor_auth.user.CreateUserRequestModel
import com.payfunds.wallet.network.response_model.two_factor_auth.check_wallet_address.TwoFactorAuthCheckWalletAddressResponseModel
import com.payfunds.wallet.network.response_model.two_factor_auth.user.CreateUserResponseModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactive.asFlow

class ManageAccountsViewModel(
    private val accountManager: IAccountManager,
    private val mode: ManageAccountsModule.Mode
) : ViewModel() {

    var viewItems by mutableStateOf<Pair<List<AccountViewItem>, List<AccountViewItem>>?>(null)
    var finish by mutableStateOf(false)

    var createUserResponseModel = mutableStateOf<CreateUserResponseModel?>(null)
    var twoFactorAuthCheckWalletAddressResponseModel =
        mutableStateOf<TwoFactorAuthCheckWalletAddressResponseModel?>(null)
    val tokenManager = CrateUserTokenManager(App.instance)

    var apiError = mutableStateOf<String?>(null)
    var isApiLoading = mutableStateOf(false)

    init {

        viewModelScope.launch {
            accountManager.accountsFlowable.asFlow()
                .collect {
                    updateViewItems(accountManager.activeAccount, it)
                }
        }

        viewModelScope.launch {
            accountManager.activeAccountStateFlow
                .collect { activeAccountState ->
                    if (activeAccountState is ActiveAccountState.ActiveAccount) {
                        updateViewItems(activeAccountState.account, accountManager.accounts)
                    }
                }
        }

        updateViewItems(accountManager.activeAccount, accountManager.accounts)
    }

    private fun updateViewItems(activeAccount: Account?, accounts: List<Account>) {
        viewItems = accounts
            .sortedBy { it.name.lowercase() }
            .map { getViewItem(it, activeAccount) }
            .partition { !it.isWatchAccount }
    }

    private fun getViewItem(account: Account, activeAccount: Account?) =
        AccountViewItem(
            accountId = account.id,
            title = account.name,
            subtitle = account.type.detailedDescription,
            selected = account == activeAccount,
            backupRequired = !account.isBackedUp && !account.isFileBackedUp,
            showAlertIcon = !account.isBackedUp || account.nonStandard || account.nonRecommended,
            isWatchAccount = account.isWatchAccount,
            migrationRequired = account.nonStandard,
        )

    fun onSelect(accountViewItem: AccountViewItem) {
        accountManager.setActiveAccountId(accountViewItem.accountId)

        val account = App.accountManager.activeAccount
        account?.let {
            val address = getWalletAddress(account)
            address?.let { walletAddress ->
                createUser(walletAddress)
            }
        }
    }

    private fun createUser(walletAddress: String, referralCode: String? = null) {

        isApiLoading.value = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = PayFundRetrofitInstance.payFundApi.createUser(
                    CreateUserRequestModel(
                        walletAddress = walletAddress.lowercase(),
                        listOf(),
                        referralCode = referralCode
                    )
                )
                if (result.isSuccessful && result.body() != null) {
                    createUserResponseModel.value = result.body()

                    createUserResponseModel.value?.let {
                        DashboardObject.updateIsDashboardOpened(true)
                        DashboardObject.updateIsMultiFactor(it.data.isMultiFactor)
                        DashboardObject.update2FAEnabled(it.data.isMultiFactor)
                    }

                    if (result.body()!!.data.token.isNotEmpty()) {
                        tokenManager.createUserSaveToken(result.body()!!.data.token)
                    }
                } else {
                    apiError.value = result.body()?.message
                }
            } catch (e: Exception) {

            } finally {
                isApiLoading.value = false
                if (mode == ManageAccountsModule.Mode.Switcher) {
                    finish = true
                }
            }
        }
    }

    fun checkWalletAddress() {
        isApiLoading.value = true

        val accountList: ArrayList<String> = ArrayList()

        val accounts = App.accountManager.accounts
        accounts.forEach { account ->
            account.let {
                val address = getWalletAddress(it)
                address?.let { walletAddress ->
                    accountList.add(walletAddress.lowercase())
                }
            }
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = PayFundRetrofitInstance.payFundApi.twoFactorAuthWalletsCheck(
                    TwoFactorAuthCheckWalletAddressRequestModel(
                        walletAddresses = accountList
                    )
                )
                if (result.isSuccessful && result.body() != null) {
                    twoFactorAuthCheckWalletAddressResponseModel.value = result.body()

                } else {
                    apiError.value = result.body()?.message
                }
            } catch (e: Exception) {

            } finally {
                isApiLoading.value = false
            }
        }
    }

}
