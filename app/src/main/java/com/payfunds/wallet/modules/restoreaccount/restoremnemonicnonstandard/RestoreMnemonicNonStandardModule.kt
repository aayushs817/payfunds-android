package com.payfunds.wallet.modules.restoreaccount.restoremnemonicnonstandard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import io.horizontalsystems.hdwalletkit.Language
import com.payfunds.wallet.core.App
import com.payfunds.wallet.entities.AccountType
import com.payfunds.wallet.modules.restoreaccount.restoremnemonic.RestoreMnemonicModule

object RestoreMnemonicNonStandardModule {

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return RestoreMnemonicNonStandardViewModel(
                App.accountFactory,
                App.wordsManager,
                App.thirdKeyboardStorage,
            ) as T
        }
    }

    data class UiState(
        val passphraseEnabled: Boolean,
        val passphraseError: String?,
        val invalidWordRanges: List<IntRange>,
        val error: String?,
        val accountType: AccountType?,
        val wordSuggestions: RestoreMnemonicModule.WordSuggestions?,
        val language: Language,
    )
}
