package com.payfunds.wallet.modules.nft.send

import android.os.Parcelable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import io.horizontalsystems.nftkit.models.NftType
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.requireInput
import com.payfunds.wallet.entities.nft.EvmNftRecord
import com.payfunds.wallet.entities.nft.NftKey
import com.payfunds.wallet.entities.nft.NftUid
import com.payfunds.wallet.modules.address.AddressInputModule
import com.payfunds.wallet.modules.address.AddressParserViewModel
import com.payfunds.wallet.modules.address.AddressViewModel
import com.payfunds.wallet.modules.send.evm.SendEvmAddressService
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.TranslatableString
import com.payfunds.wallet.ui.compose.components.AppBar
import com.payfunds.wallet.ui.compose.components.MenuItem
import com.payfunds.wallet.ui.compose.components.ScreenMessageWithAction
import kotlinx.parcelize.Parcelize

class SendNftFragment : BaseComposeFragment() {

    @Parcelize
    data class Input(val nftUid: String) : Parcelable

    @Composable
    override fun GetContent(navController: NavController) {
        val factory = getFactory(navController.requireInput<Input>().nftUid)

        when (factory?.evmNftRecord?.nftType) {
            NftType.Eip721 -> {
                val eip721ViewModel by viewModels<SendEip721ViewModel> { factory }
                val addressViewModel by viewModels<AddressViewModel> {
                    AddressInputModule.FactoryNft(factory.nftUid.blockchainType)
                }
                val addressParserViewModel by viewModels<AddressParserViewModel> { factory }
                SendEip721Screen(
                    navController,
                    eip721ViewModel,
                    addressViewModel,
                    addressParserViewModel,
                )
            }

            NftType.Eip1155 -> {
                val eip1155ViewModel by viewModels<SendEip1155ViewModel> { factory }
                val addressViewModel by viewModels<AddressViewModel> {
                    AddressInputModule.FactoryNft(factory.nftUid.blockchainType)
                }
                val addressParserViewModel by viewModels<AddressParserViewModel> { factory }
                SendEip1155Screen(
                    navController,
                    eip1155ViewModel,
                    addressViewModel,
                    addressParserViewModel,
                )
            }

            else -> {
                ShowErrorMessage(navController)
            }
        }
    }

}

private fun getFactory(nftUidString: String): SendNftModule.Factory? {
    val nftUid = NftUid.fromUid(nftUidString)

    val account = App.accountManager.activeAccount ?: return null

    if (account.isWatchAccount) return null

    val nftKey = NftKey(account, nftUid.blockchainType)

    val adapter = App.nftAdapterManager.adapter(nftKey) ?: return null

    val nftRecord = adapter.nftRecord(nftUid) ?: return null

    val evmNftRecord = (nftRecord as? EvmNftRecord) ?: return null

    return SendNftModule.Factory(
        evmNftRecord,
        nftUid,
        nftRecord.balance,
        adapter,
        SendEvmAddressService(),
        App.nftMetadataManager
    )
}

@Composable
private fun ShowErrorMessage(navController: NavController) {
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = stringResource(R.string.SendNft_Title),
                menuItems = listOf(
                    MenuItem(
                        title = TranslatableString.ResString(R.string.Button_Close),
                        icon = R.drawable.ic_close,
                        onClick = { navController.popBackStack() }
                    )
                )
            )
        }
    ) {
        Column(Modifier.padding(it)) {
            ScreenMessageWithAction(
                text = stringResource(R.string.Error),
                icon = R.drawable.ic_error_48
            )
        }
    }
}
