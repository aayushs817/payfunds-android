package com.payfunds.wallet.modules.manageaccount.backupkey

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.getInput
import com.payfunds.wallet.core.slideFromRight
import com.payfunds.wallet.entities.Account
import com.payfunds.wallet.modules.evmfee.ButtonsGroupWithShade
import com.payfunds.wallet.modules.manageaccount.ui.PassphraseCell
import com.payfunds.wallet.modules.manageaccount.ui.SeedPhraseList
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.TranslatableString
import com.payfunds.wallet.ui.compose.components.AppBar
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.InfoText
import com.payfunds.wallet.ui.compose.components.MenuItem

class BackupKeyFragment : BaseComposeFragment(screenshotEnabled = false) {

    @Composable
    override fun GetContent(navController: NavController) {
        val account = navController.getInput<Account>()
        if (account == null) {
            navController.popBackStack(R.id.mainFragment, false)
            return
        }
        RecoveryPhraseScreen(navController, account)
    }

}

@Composable
fun RecoveryPhraseScreen(
    navController: NavController,
    account: Account
) {
    val viewModel = viewModel<BackupKeyViewModel>(factory = BackupKeyModule.Factory(account))

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = stringResource(R.string.RecoveryPhrase_Title),
                menuItems = listOf(
//                    MenuItem(
//                        title = TranslatableString.ResString(R.string.Info_Title),
//                        icon = R.drawable.ic_info_24,
//                        onClick = {
//                            FaqManager.showFaqPage(navController, FaqManager.faqPathPrivateKeys)
//                        }
//                    ),
                    MenuItem(
                        title = TranslatableString.ResString(R.string.Button_Close),
                        icon = R.drawable.ic_close,
                        onClick = {
                            navController.popBackStack()
                        }
                    )
                )
            )
        }
    ) {
        Column(modifier = Modifier.padding(it)) {
            var hidden by remember { mutableStateOf(true) }

            InfoText(text = stringResource(R.string.RecoveryPhrase_Description))
            Spacer(Modifier.height(12.dp))
            SeedPhraseList(
                wordsNumbered = viewModel.wordsNumbered,
                hidden = hidden
            ) {
                hidden = !hidden
            }
            Spacer(Modifier.height(24.dp))
            PassphraseCell(viewModel.passphrase, hidden)
            Spacer(modifier = Modifier.weight(1f))
            ButtonsGroupWithShade {
                ButtonPrimaryRed(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp),
                    title = stringResource(R.string.RecoveryPhrase_Verify),
                    onClick = {
                        navController.slideFromRight(
                            R.id.backupConfirmationKeyFragment,
                            viewModel.account
                        )
                    },
                )
            }
        }
    }
}
