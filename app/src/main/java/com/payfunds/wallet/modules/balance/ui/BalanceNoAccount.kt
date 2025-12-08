package com.payfunds.wallet.modules.balance.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.navigateWithTermsAccepted
import com.payfunds.wallet.core.slideFromRight
import com.payfunds.wallet.core.stats.StatEvent
import com.payfunds.wallet.core.stats.StatPage
import com.payfunds.wallet.core.stats.stat
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryTransparent
import com.payfunds.wallet.ui.compose.components.headline1_leah

@Composable
fun BalanceNoAccount(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            painter = painterResource(R.drawable.img_add_to_walle_2),
            contentDescription = "",
        )
        Spacer(Modifier.height(48.dp))

        headline1_leah(
            modifier = Modifier.padding(horizontal = 48.dp),
            text = stringResource(R.string.ManageAccounts_ImportWalletDescription)
        )
        Spacer(Modifier.height(48.dp))

        ButtonPrimaryRed(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            title = stringResource(R.string.ManageAccounts_CreateWallet),
            onClick = {
                navController.navigateWithTermsAccepted {
                    navController.slideFromRight(R.id.createAccountFragment)

                    stat(page = StatPage.Balance, event = StatEvent.Open(StatPage.NewWallet))
                }
            }
        )
        Spacer(modifier = Modifier.height(16.dp))
        ButtonPrimaryTransparent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp),
            border = BorderStroke(1.dp, ComposeAppTheme.colors.steel20),
            title = stringResource(R.string.ManageAccounts_ImportWallet),
            onClick = {
                navController.navigateWithTermsAccepted {
                    navController.slideFromRight(R.id.importWalletFragment)

                    stat(page = StatPage.Balance, event = StatEvent.Open(StatPage.ImportWallet))
                }
            }
        )
        /* Spacer(modifier = Modifier.height(16.dp))
         ButtonPrimaryTransparent(
             modifier = Modifier
                 .fillMaxWidth()
                 .padding(horizontal = 48.dp),
             title = stringResource(R.string.ManageAccounts_WatchAddress),
             onClick = {
                 navController.slideFromRight(R.id.watchAddressFragment)
 
                 stat(page = StatPage.Balance, event = StatEvent.Open(StatPage.WatchWallet))
             }
         */
    }
}

@Preview
@Composable
fun BalanceNoAccountPreview() {
    ComposeAppTheme {
        BalanceNoAccount(navController = NavController(LocalContext.current))
    }
}