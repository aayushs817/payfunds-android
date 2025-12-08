package com.payfunds.wallet.modules.createaccount

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.DashboardObject
import com.payfunds.wallet.core.IAccountFactory
import com.payfunds.wallet.core.IAccountManager
import com.payfunds.wallet.core.getWalletAddress
import com.payfunds.wallet.core.managers.PassphraseValidator
import com.payfunds.wallet.core.managers.WalletActivator
import com.payfunds.wallet.core.managers.WordsManager
import com.payfunds.wallet.core.providers.PredefinedBlockchainSettingsProvider
import com.payfunds.wallet.core.providers.Translator
import com.payfunds.wallet.entities.Account
import com.payfunds.wallet.entities.AccountOrigin
import com.payfunds.wallet.entities.AccountType
import com.payfunds.wallet.entities.DataState
import com.payfunds.wallet.entities.normalizeNFKD
import com.payfunds.wallet.modules.createaccount.CreateAccountModule.Kind.Mnemonic12
import com.payfunds.wallet.modules.settings.security.twofactorauth.CrateUserTokenManager
import com.payfunds.wallet.network.PayFundRetrofitInstance
import com.payfunds.wallet.network.request_model.remove_fcm.RemoveFcmRequestModel
import com.payfunds.wallet.network.request_model.two_factor_auth.user.CreateUserRequestModel
import com.payfunds.wallet.network.response_model.two_factor_auth.user.CreateUserResponseModel
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.TokenQuery
import io.horizontalsystems.marketkit.models.TokenType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CreateAccountViewModel(
    private val accountFactory: IAccountFactory,
    private val wordsManager: WordsManager,
    private val accountManager: IAccountManager,
    private val walletActivator: WalletActivator,
    private val passphraseValidator: PassphraseValidator,
    private val predefinedBlockchainSettingsProvider: PredefinedBlockchainSettingsProvider,
) : ViewModel() {

    private var passphrase = ""
    private var passphraseConfirmation = ""

    val mnemonicKinds = CreateAccountModule.Kind.values().toList()

    val defaultAccountName = accountFactory.getNextAccountName()
    var accountName: String = defaultAccountName
        get() = field.ifBlank { defaultAccountName }
        private set

    var selectedKind: CreateAccountModule.Kind = Mnemonic12
        private set

    var passphraseEnabled by mutableStateOf(false)
        private set

    var passphraseConfirmState by mutableStateOf<DataState.Error?>(null)
        private set

    var passphraseState by mutableStateOf<DataState.Error?>(null)
        private set

    var success by mutableStateOf<AccountType?>(null)
        private set

    var apiError = mutableStateOf<String?>(null)
    var isLoading = mutableStateOf(false)

    private val tokenManager = CrateUserTokenManager(App.instance)
    var createUserResponseModel = mutableStateOf<CreateUserResponseModel?>(null)

    fun createAccount(referralCode: String? = null) {
        if (passphraseEnabled && passphraseIsInvalid()) {
            return
        }

        val accountType = mnemonicAccountType(selectedKind.wordsCount)
        val account = accountFactory.account(
            accountName,
            accountType,
            AccountOrigin.Created,
            false,
            false,
        )


        val address = getWalletAddress(account)
        address?.let {
            viewModelScope.launch(Dispatchers.IO) {

                try {
                    val result = PayFundRetrofitInstance.payFundApi.createUser(
                        CreateUserRequestModel(
                            address.lowercase(),
                            listOf(), referralCode
                        )
                    )
                    if (result.isSuccessful && result.body() != null) {
                        isLoading.value = false
                        accountManager.save(account)
                        activateDefaultWallets(account)
                        predefinedBlockchainSettingsProvider.prepareNew(
                            account,
                            BlockchainType.Zcash
                        )
                        success = accountType
                        createUserResponseModel.value = result.body()
                        DashboardObject.updateIsDashboardOpened(true)
                        DashboardObject.updateIsMultiFactor(result.body()!!.data.isMultiFactor)
                        if (result.body()!!.data.token.isNotEmpty()) {
                            tokenManager.createUserSaveToken(result.body()!!.data.token)
                        }

                    } else {
                        success?.let {
                            onUnlink(account)
                        }
                        isLoading.value = false
                        result.errorBody()
                    }
                } catch (e: Exception) {
                    isLoading.value = false
                    e.printStackTrace()
                }
            }
        }

        accountManager.save(account)
        activateDefaultWallets(account)
        predefinedBlockchainSettingsProvider.prepareNew(account, BlockchainType.Zcash)
        success = accountType
    }


    fun onUnlink(account: Account) {
        accountManager.delete(account.id)
        removeFcmToken(account)
    }

    private fun removeFcmToken(account: Account) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val result = PayFundRetrofitInstance.payFundApi.removeFcmToken(
                    RemoveFcmRequestModel(
                        walletAddress = getWalletAddress(account)?.lowercase() ?: "",
                        deviceId = DashboardObject.deviceId,
                        os = "android"
                    )
                )
                if (result.isSuccessful && result.body() != null) {
                    val responseBody = result.body()!!
                } else {
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun onChangeAccountName(name: String) {
        accountName = name
    }

    fun onChangePassphrase(v: String) {
        if (passphraseValidator.containsValidCharacters(v)) {
            passphraseState = null
            passphrase = v
        } else {
            passphraseState = DataState.Error(
                Exception(
                    Translator.getString(R.string.CreateWallet_Error_PassphraseForbiddenSymbols)
                )
            )
        }
    }

    fun onChangePassphraseConfirmation(v: String) {
        passphraseConfirmState = null
        passphraseConfirmation = v
    }

    fun setMnemonicKind(kind: CreateAccountModule.Kind) {
        selectedKind = kind
    }

    fun setPassphraseEnabledState(enabled: Boolean) {
        passphraseEnabled = enabled
        if (!enabled) {
            passphrase = ""
            passphraseConfirmation = ""
        }
    }

    fun onSuccessMessageShown() {
        success = null
    }

    private fun passphraseIsInvalid(): Boolean {
        if (passphraseState is DataState.Error) {
            return true
        }

        if (passphrase.isBlank()) {
            passphraseState = DataState.Error(
                Exception(
                    Translator.getString(R.string.CreateWallet_Error_EmptyPassphrase)
                )
            )
            return true
        }
        if (passphrase != passphraseConfirmation) {
            passphraseConfirmState = DataState.Error(
                Exception(
                    Translator.getString(R.string.CreateWallet_Error_InvalidConfirmation)
                )
            )
            return true
        }
        return false
    }

    private fun activateDefaultWallets(account: Account) {
        val tokenQueries = listOfNotNull(
            TokenQuery(BlockchainType.Bitcoin, TokenType.Derived(TokenType.Derivation.Bip84)),
            TokenQuery(BlockchainType.Ethereum, TokenType.Native),
            TokenQuery(BlockchainType.BinanceSmartChain, TokenType.Native),
            TokenQuery(
                BlockchainType.Ethereum,
                TokenType.Eip20("0xdac17f958d2ee523a2206206994597c13d831ec7")
            ),
            TokenQuery(
                BlockchainType.Tron,
                TokenType.Eip20("TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t")
            ),
        )
        walletActivator.activateWallets(account, tokenQueries)
    }

    private fun mnemonicAccountType(wordCount: Int): AccountType {
        // A new account can be created only using an English wordlist and limited chars in the passphrase.
        // Despite it, we add text normalizing.
        // It is to avoid potential issues if we allow non-English wordlists on account creation.
        val words = wordsManager.generateWords(wordCount).map { it.normalizeNFKD() }
        return AccountType.Mnemonic(words, passphrase.normalizeNFKD())
    }


}
