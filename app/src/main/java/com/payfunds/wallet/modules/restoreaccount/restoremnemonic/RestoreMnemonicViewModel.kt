package com.payfunds.wallet.modules.restoreaccount.restoremnemonic

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.viewModelScope
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.DashboardObject
import com.payfunds.wallet.core.IAccountFactory
import com.payfunds.wallet.core.IAccountManager
import com.payfunds.wallet.core.ViewModelUiState
import com.payfunds.wallet.core.getWalletAddress
import com.payfunds.wallet.core.managers.WordsManager
import com.payfunds.wallet.core.providers.Translator
import com.payfunds.wallet.entities.Account
import com.payfunds.wallet.entities.AccountType
import com.payfunds.wallet.entities.normalizeNFKD
import com.payfunds.wallet.modules.restoreaccount.restoremnemonic.RestoreMnemonicModule.UiState
import com.payfunds.wallet.modules.restoreaccount.restoremnemonic.RestoreMnemonicModule.WordItem
import com.payfunds.wallet.modules.settings.security.twofactorauth.CrateUserTokenManager
import com.payfunds.wallet.network.PayFundRetrofitInstance
import com.payfunds.wallet.network.request_model.remove_fcm.RemoveFcmRequestModel
import com.payfunds.wallet.network.request_model.two_factor_auth.user.CreateUserRequestModel
import com.payfunds.wallet.network.response_model.two_factor_auth.user.CreateUserResponseModel
import io.horizontalsystems.ethereumkit.models.Chain
import io.horizontalsystems.hdwalletkit.Language
import io.horizontalsystems.hdwalletkit.Mnemonic
import io.horizontalsystems.hdwalletkit.WordList
import io.payfunds.core.CoreApp
import io.payfunds.core.IThirdKeyboard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class RestoreMnemonicViewModel(
    accountFactory: IAccountFactory,
    private val wordsManager: WordsManager,
    private val thirdKeyboardStorage: IThirdKeyboard,
    private val accountManager: IAccountManager
) : ViewModelUiState<UiState>() {

    val mnemonicLanguages = Language.values().toList()

    private var passphraseEnabled: Boolean = false
    private var passphrase: String = ""
    private var passphraseError: String? = null
    private var wordItems: List<WordItem> = listOf()
    private var invalidWordItems: List<WordItem> = listOf()
    private var invalidWordRanges: List<IntRange> = listOf()
    private var error: String? = null
    private var accountType: AccountType? = null
    private var wordSuggestions: RestoreMnemonicModule.WordSuggestions? = null
    private var language = Language.English
    private var text = ""
    private var cursorPosition = 0
    private var mnemonicWordList = WordList.wordListStrict(language)


    private val regex = Regex("\\S+")

    val defaultName = accountFactory.getNextAccountName()
    var accountName: String = defaultName
        get() = field.ifBlank { defaultName }
        private set


    val isThirdPartyKeyboardAllowed: Boolean
        get() = CoreApp.thirdKeyboardStorage.isThirdPartyKeyboardAllowed

    var createAccountResponseModel = mutableStateOf<CreateUserResponseModel?>(null)

    val tokenManager = CrateUserTokenManager(App.instance)


    var createAccountJob: Job? = null

    override fun createState() = UiState(
        passphraseEnabled = passphraseEnabled,
        passphraseError = passphraseError,
        invalidWordRanges = invalidWordRanges,
        error = error,
        accountType = accountType,
        wordSuggestions = wordSuggestions,
        language = language,
    )

    private fun processText() {
        wordItems = wordItems(text)
        invalidWordItems =
            wordItems.filter { !mnemonicWordList.validWord(it.word.normalizeNFKD(), false) }

        val wordItemWithCursor = wordItems.find {
            it.range.contains(cursorPosition - 1)
        }

        val invalidWordItemsExcludingCursoredPartiallyValid = when {
            wordItemWithCursor != null && mnemonicWordList.validWord(
                wordItemWithCursor.word.normalizeNFKD(),
                true
            ) -> {
                invalidWordItems.filter { it != wordItemWithCursor }
            }

            else -> invalidWordItems
        }

        invalidWordRanges = invalidWordItemsExcludingCursoredPartiallyValid.map { it.range }
        wordSuggestions = wordItemWithCursor?.let {
            RestoreMnemonicModule.WordSuggestions(
                it,
                mnemonicWordList.fetchSuggestions(it.word.normalizeNFKD())
            )
        }
    }

    fun onTogglePassphrase(enabled: Boolean) {
        passphraseEnabled = enabled
        passphrase = ""
        passphraseError = null
        passphraseError = null

        emitState()
    }

    fun onEnterPassphrase(passphrase: String) {
        this.passphrase = passphrase
        passphraseError = null

        emitState()
    }

    fun onEnterName(name: String) {
        accountName = name
    }

    fun onEnterMnemonicPhrase(text: String, cursorPosition: Int) {
        error = null
        this.text = text
        this.cursorPosition = cursorPosition
        processText()

        emitState()
    }

    fun setMnemonicLanguage(language: Language) {
        this.language = language
        mnemonicWordList = WordList.wordListStrict(language)
        processText()

        emitState()
    }

    fun onProceed() {
        when {
            invalidWordItems.isNotEmpty() -> {
                invalidWordRanges = invalidWordItems.map { it.range }
            }

            wordItems.size !in (Mnemonic.EntropyStrength.values().map { it.wordCount }) -> {
                error =
                    Translator.getString(R.string.Restore_Error_MnemonicWordCount, wordItems.size)
            }

            passphraseEnabled && passphrase.isBlank() -> {
                passphraseError = Translator.getString(R.string.Restore_Error_EmptyPassphrase)
            }

            else -> {
                try {
                    val words = wordItems.map { it.word.normalizeNFKD() }
                    wordsManager.validateChecksumStrict(words)

                    accountType = AccountType.Mnemonic(words, passphrase.normalizeNFKD())
                    error = null
                } catch (checksumException: Exception) {
                    error = Translator.getString(R.string.Restore_InvalidChecksum)
                }
            }
        }

        emitState()
    }

    fun createAccount(accountType: AccountType, referralCode: String? = null) {

        val address = accountType.evmAddress(Chain.Ethereum)?.hex
        address?.let {
            createAccountJob?.cancel()
            createAccountJob = viewModelScope.launch(Dispatchers.IO) {

                val account = accountManager.accounts.find {
                    it.type.evmAddress(Chain.Ethereum)?.hex.equals(
                        address,
                        true
                    )
                }

                try {
                    val result = PayFundRetrofitInstance.payFundApi.createUser(
                        CreateUserRequestModel(
                            address.lowercase(),
                            listOf(),
                            referralCode
                        )
                    )
                    if (result.isSuccessful && result.body() != null) {
                        createAccountResponseModel.value = result.body()
                    } else {

                        account?.let {
                            onUnlink(account)
                        }
                        val responseBody = result.body()!!
                        error = responseBody.message
                    }
                } catch (e: Exception) {
                    account?.let {
                        onUnlink(account)
                    }
                    error = "Something went wrong"
                    e.printStackTrace()
                }
            }
        }

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

    fun onSelectCoinsShown() {
        accountType = null

        emitState()
    }

    fun onAllowThirdPartyKeyboard() {
        thirdKeyboardStorage.isThirdPartyKeyboardAllowed = true
    }

    private fun wordItems(text: String): List<WordItem> {
        return regex.findAll(text.lowercase())
            .map { WordItem(it.value, it.range) }
            .toList()
    }
}
