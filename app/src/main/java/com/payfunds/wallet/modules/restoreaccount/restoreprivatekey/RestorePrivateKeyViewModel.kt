package com.payfunds.wallet.modules.restoreaccount.restoreprivatekey

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App.Companion.accountManager
import com.payfunds.wallet.core.DashboardObject
import com.payfunds.wallet.core.IAccountFactory
import com.payfunds.wallet.core.getWalletAddress
import com.payfunds.wallet.core.providers.Translator
import com.payfunds.wallet.entities.Account
import com.payfunds.wallet.entities.AccountType
import com.payfunds.wallet.entities.DataState
import com.payfunds.wallet.modules.restoreaccount.restoreprivatekey.RestorePrivateKeyModule.RestoreError.EmptyText
import com.payfunds.wallet.modules.restoreaccount.restoreprivatekey.RestorePrivateKeyModule.RestoreError.NoValidKey
import com.payfunds.wallet.modules.restoreaccount.restoreprivatekey.RestorePrivateKeyModule.RestoreError.NonPrivateKey
import com.payfunds.wallet.modules.restoreaccount.restoreprivatekey.RestorePrivateKeyModule.RestoreError.NotSupportedDerivedType
import com.payfunds.wallet.network.PayFundRetrofitInstance
import com.payfunds.wallet.network.request_model.remove_fcm.RemoveFcmRequestModel
import com.payfunds.wallet.network.request_model.two_factor_auth.user.CreateUserRequestModel
import io.horizontalsystems.ethereumkit.core.signer.Signer
import io.horizontalsystems.ethereumkit.models.Chain
import io.horizontalsystems.hdwalletkit.HDExtendedKey
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.math.BigInteger

class RestorePrivateKeyViewModel(
    accountFactory: IAccountFactory,
) : ViewModel() {

    val defaultName = accountFactory.getNextAccountName()
    var accountName: String = defaultName
        get() = field.ifBlank { defaultName }
        private set

    private var text = ""

    var inputState by mutableStateOf<DataState.Error?>(null)
        private set

    fun onEnterName(name: String) {
        accountName = name
    }

    fun onEnterPrivateKey(input: String) {
        inputState = null
        text = input
    }

    fun resolveAccountType(): AccountType? {
        inputState = null
        return try {
            accountType(text)
        } catch (e: Exception) {
            inputState = DataState.Error(
                Exception(Translator.getString(R.string.Restore_PrivateKey_InvalidKey))
            )
            null
        }
    }

    fun createAccount(accountType: AccountType, referralCode: String? = null) {

        val address = accountType.evmAddress(Chain.Ethereum)?.hex
        address?.let {

            viewModelScope.launch(Dispatchers.IO) {

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
                            listOf(), referralCode
                        )
                    )
                    if (result.isSuccessful && result.body() != null) {
                        DashboardObject.updateIsDashboardOpened(true)
                        DashboardObject.updateIsMultiFactor(result.body()!!.data.isMultiFactor)
                    } else {
                        account?.let {
                            onUnlink(account)
                        }
                    }
                } catch (e: Exception) {
                    account?.let {
                        onUnlink(account)
                    }
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

    @Throws(Exception::class)
    private fun accountType(text: String): AccountType {
        val textCleaned = text.trim()

        if (textCleaned.isEmpty()) {
            throw EmptyText
        }

        if (isValidEthereumPrivateKey(textCleaned)) {
            val privateKey = Signer.privateKey(textCleaned)
            return AccountType.EvmPrivateKey(privateKey)
        }

        try {
            val extendedKey = HDExtendedKey(textCleaned)
            if (extendedKey.isPublic) {
                throw NonPrivateKey
            }
            when (extendedKey.derivedType) {
                HDExtendedKey.DerivedType.Master,
                HDExtendedKey.DerivedType.Account -> {
                    return AccountType.HdExtendedKey(extendedKey.serializePrivate())
                }

                else -> throw NotSupportedDerivedType
            }
        } catch (e: Throwable) {
            throw NoValidKey
        }
    }

    @OptIn(ExperimentalStdlibApi::class)
    private fun isValidEthereumPrivateKey(privateKeyHex: String): Boolean {
        try {
            //key should be 32 bytes long
            privateKeyHex.hexToByteArray().let {
                if (it.size != 32) {
                    return false
                }
            }

            // Convert the hex private key to a BigInteger
            val privateKeyBigInt = BigInteger(privateKeyHex, 16)

            // Define the order of the secp256k1 curve (n)
            val secp256k1Order = BigInteger(
                "fffffffffffffffffffffffffffffffebaaedce6af48a03bbfd25e8cd0364141",
                16
            )

            // Check if the private key is greater than zero and less than the order
            return privateKeyBigInt > BigInteger.ZERO && privateKeyBigInt < secp256k1Order
        } catch (e: NumberFormatException) {
            return false
        }
    }
}