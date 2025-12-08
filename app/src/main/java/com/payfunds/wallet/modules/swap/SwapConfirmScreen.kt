package com.payfunds.wallet.modules.swap

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.payfunds.wallet.R
import com.payfunds.wallet.modules.evmfee.ButtonsGroupWithShade
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.TranslatableString
import com.payfunds.wallet.ui.compose.components.AppBar
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.FormsInputSearch
import com.payfunds.wallet.ui.compose.components.HsBackButton
import com.payfunds.wallet.ui.compose.components.MenuItem
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.compose.components.caption_grey
import com.payfunds.wallet.ui.compose.components.headline2_leah
import com.payfunds.wallet.ui.compose.components.subhead1_leah
import com.payfunds.wallet.ui.compose.components.subhead2_grey


@Composable
fun SwapConfirmScreen(
    navController: NavHostController,
) {
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        backgroundColor = ComposeAppTheme.colors.tyler, topBar = {
        AppBar(
            title = stringResource(R.string.SwapToken_Title), navigationIcon = {
                HsBackButton(onClick = { navController.popBackStack() })
            }, menuItems = listOf(
                MenuItem(title = TranslatableString.ResString(R.string.Settings_Title),
                    icon = R.drawable.ic_settings,
                    tint = ComposeAppTheme.colors.grey,
                    onClick = {
                        navController.navigate("swapPayScreen")
                    })
            )
        )
    }, bottomBar = {
        ButtonsGroupWithShade {
            ButtonPrimaryRed(modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 16.dp),
                title = stringResource(R.string.Swap),
                onClick = {
                    navController.navigate("swapGetScreen")
                })
        }
    }) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
        ) {
            VSpacer(12.dp)

            headline2_leah(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(R.string.Swap_Youre_swapping),
            )
            VSpacer(24.dp)
            FormsInputSearch(
                modifier = Modifier.padding(horizontal = 12.dp),
                onValueChange = {},
                hint = "0.005wETH",
                enabled = true,
                singleLine = true,
                endContent = {
                    Image(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(24.dp),
                        painter = painterResource(R.drawable.bep20),
                        contentDescription = null,
                    )
                }
            )
            VSpacer(12.dp)

            headline2_leah(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                text = stringResource(R.string.Swap_To),
                textAlign = TextAlign.Center
            )

            VSpacer(12.dp)

            FormsInputSearch(
                modifier = Modifier.padding(horizontal = 12.dp),
                hint = "15678.22 SHIB",
                enabled = true,
                singleLine = true,
                endContent = {
                    Image(
                        modifier = Modifier
                            .padding(start = 12.dp)
                            .size(24.dp),
                        painter = painterResource(R.drawable.base_erc20),
                        contentDescription = null,
                    )
                },
                onValueChange = {}
            )

            VSpacer(24.dp)
            Divider(
                thickness = 1.dp,
                modifier = Modifier.padding(horizontal = 16.dp),
                color = ComposeAppTheme.colors.steel10
            )
            VSpacer(24.dp)

            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ComposeAppTheme.colors.lawrence)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        subhead1_leah(text = stringResource(R.string.swap_network_fee))
                        Icon(
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(16.dp),
                            painter = painterResource(R.drawable.ic_info_20),
                            contentDescription = null,
                            tint = ComposeAppTheme.colors.grey
                        )
                    }
                    subhead2_grey(text = "0.026 POL")
                }
                VSpacer(8.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    subhead1_leah(text = stringResource(R.string.swap_fee))
                    subhead2_grey(text = "0.005 POL (\$6.95 USD)")
                }
                VSpacer(8.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    subhead1_leah(text = stringResource(R.string.swap_rate))
                    subhead2_grey(text = "0.005 POL (\$6.95 USD)")
                }
                VSpacer(8.dp)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        subhead1_leah(text = stringResource(R.string.swap_max_slippage))
                        Icon(
                            modifier = Modifier
                                .padding(start = 4.dp)
                                .size(16.dp),
                            painter = painterResource(R.drawable.ic_info_20),
                            contentDescription = null,
                            tint = ComposeAppTheme.colors.grey
                        )
                    }
                    subhead2_grey(text = "0.50%")
                }
                VSpacer(6.dp)
               caption_grey(
                    modifier = Modifier.fillMaxWidth(),
                    text = "You are guaranteed to receive: 15000 SHIB",
                    textAlign = TextAlign.End
                )

            }
            VSpacer(32.dp)
        }
    }
    if (showDialog) {
        SwapSettingsDialog(
            onCloseClick = { showDialog = false },
            onCancelClick = { showDialog = false },
        )
    }
}

@Preview
@Composable
fun SwapConfirmScreenPreview() {
    ComposeAppTheme {
        SwapConfirmScreen(navController = rememberNavController())
    }
}