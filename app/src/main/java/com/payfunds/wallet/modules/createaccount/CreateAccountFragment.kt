package com.payfunds.wallet.modules.createaccount

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.composablePage
import com.payfunds.wallet.core.getInput
import com.payfunds.wallet.core.stats.StatEvent
import com.payfunds.wallet.core.stats.StatPage
import com.payfunds.wallet.core.stats.stat
import com.payfunds.wallet.core.stats.statAccountType
import com.payfunds.wallet.modules.manageaccounts.ManageAccountsModule
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.AppBar
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryTransparent
import com.payfunds.wallet.ui.compose.components.FormsInput
import com.payfunds.wallet.ui.compose.components.HeaderText
import com.payfunds.wallet.ui.compose.components.HsBackButton
import io.payfunds.core.helpers.HudHelper
import kotlinx.coroutines.delay

class CreateAccountFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        val input = navController.getInput<ManageAccountsModule.Input>()
        val popUpToInclusiveId = input?.popOffOnSuccess ?: R.id.createAccountFragment
        val inclusive = input?.popOffInclusive ?: true
        CreateAccountNavHost(navController, popUpToInclusiveId, inclusive)
    }

}

@Composable
private fun CreateAccountNavHost(
    fragmentNavController: NavController,
    popUpToInclusiveId: Int,
    inclusive: Boolean
) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = "create_account_intro",
    ) {
        composable("create_account_intro") {
            CreateAccountIntroScreen(
                openCreateAdvancedScreen = { navController.navigate("create_account_advanced") },
                onBackClick = { fragmentNavController.popBackStack() },
                onFinish = { fragmentNavController.popBackStack(popUpToInclusiveId, inclusive) },
            )
        }
        composablePage("create_account_advanced") {
            CreateAccountAdvancedScreen(
                onBackClick = { navController.popBackStack() },
                onFinish = { fragmentNavController.popBackStack(popUpToInclusiveId, inclusive) }
            )
        }
    }
}

@Composable
private fun CreateAccountIntroScreen(
    openCreateAdvancedScreen: () -> Unit,
    onBackClick: () -> Unit,
    onFinish: () -> Unit
) {
    val viewModel = viewModel<CreateAccountViewModel>(factory = CreateAccountModule.Factory())
    val view = LocalView.current



    LaunchedEffect(viewModel.success) {
        viewModel.success?.let { accountType ->
            HudHelper.showSuccessMessage(
                contenView = view,
                resId = R.string.Hud_Text_Created,
                icon = R.drawable.icon_add_to_wallet_24,
                iconTint = R.color.white
            )
            delay(300)

            onFinish.invoke()
            viewModel.onSuccessMessageShown()

            stat(
                page = StatPage.NewWallet,
                event = StatEvent.CreateWallet(accountType.statAccountType)
            )
        }
    }

    Surface(color = ComposeAppTheme.colors.tyler) {
        Column(Modifier.fillMaxSize().statusBarsPadding()) {
            AppBar(
                title = stringResource(R.string.ManageAccounts_CreateNewWallet),
   /*             menuItems = listOf(
                    MenuItem(
                        title = TranslatableString.ResString(R.string.Button_Create),
                        onClick = viewModel::createAccount
                    )
                ),*/
                navigationIcon = {
                    HsBackButton(onClick = onBackClick)
                },
                backgroundColor = Color.Transparent
            )
            Spacer(Modifier.height(12.dp))

            HeaderText(stringResource(id = R.string.ManageAccount_Name))
            FormsInput(
                modifier = Modifier.padding(horizontal = 16.dp),
                initial = viewModel.accountName,
                pasteEnabled = false,
                hint = viewModel.defaultAccountName,
                onValueChange = viewModel::onChangeAccountName
            )

            var referralCode by remember { mutableStateOf<String?>(null) }
            Spacer(modifier = Modifier.height(8.dp))
            HeaderText(stringResource(R.string.referral_code))
            FormsInput(
                modifier = Modifier.padding(horizontal = 16.dp),
                initial = referralCode,
                pasteEnabled = false,
                hint = "Referral Code",
                onValueChange = {
                    referralCode = it
                }
            )

            Spacer(Modifier.weight(1f))
            ButtonPrimaryRed(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                title = stringResource(R.string.Button_Create),
                onClick = {viewModel.createAccount(referralCode)}
            )
            Spacer(modifier = Modifier.height(16.dp))
            ButtonPrimaryTransparent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                border = BorderStroke(1.dp, ComposeAppTheme.colors.steel20),
                title = stringResource(R.string.Button_Advanced),
                onClick = {
                    openCreateAdvancedScreen.invoke()
                    stat(
                        page = StatPage.NewWallet,
                        event = StatEvent.Open(StatPage.NewWalletAdvanced)
                    )
                }
            )
            Spacer(Modifier.height(32.dp))
//
//            CellSingleLineLawrenceSection {
//                Row(
//                    modifier = Modifier
//                        .fillMaxSize()
//                        .clickable {
//                            openCreateAdvancedScreen.invoke()
//
//                            stat(
//                                page = StatPage.NewWallet,
//                                event = StatEvent.Open(StatPage.NewWalletAdvanced)
//                            )
//                        }
//                        .padding(horizontal = 16.dp),
//                    verticalAlignment = Alignment.CenterVertically,
//                ) {
//                    body_leah(text = stringResource(R.string.Button_Advanced))
//                    Spacer(modifier = Modifier.weight(1f))
//                    Image(
//                        modifier = Modifier.size(20.dp),
//                        painter = painterResource(id = R.drawable.ic_arrow_right),
//                        contentDescription = null,
//                    )
//                }
//            }
//
//            Spacer(Modifier.height(32.dp))
        }
    }
}
