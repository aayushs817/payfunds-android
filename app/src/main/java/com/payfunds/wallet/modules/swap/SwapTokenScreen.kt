package com.payfunds.wallet.modules.swap

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.payfunds.wallet.R
import com.payfunds.wallet.modules.evmfee.ButtonsGroupWithShade
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.TranslatableString
import com.payfunds.wallet.ui.compose.components.AppBar
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.FormsInput
import com.payfunds.wallet.ui.compose.components.HSpacer
import com.payfunds.wallet.ui.compose.components.HeaderSorting
import com.payfunds.wallet.ui.compose.components.HsBackButton
import com.payfunds.wallet.ui.compose.components.HsIconButton
import com.payfunds.wallet.ui.compose.components.MenuItem
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.compose.components.body_leah
import com.payfunds.wallet.ui.compose.components.subhead1_grey
import com.payfunds.wallet.ui.compose.components.subhead1_leah


data class Token(
    val icon: Int,
    val name: String
)

val TokenSaver = Saver<Token, Pair<Int, String>>(
    save = { token -> token.icon to token.name },
    restore = { (icon, name) -> Token(icon, name) }
)

@Composable
fun SwapTokenScreen(
    navController: NavController,
    onBackClick: () -> Unit,
) {
    var showDialog by remember { mutableStateOf(false) }
    var tokenOneValue by rememberSaveable { mutableStateOf("") }
    var tokenTwoValue by rememberSaveable { mutableStateOf("") }

    var tokenOne by rememberSaveable(stateSaver = TokenSaver) {
        mutableStateOf(Token(R.drawable.bep20, "ETH"))
    }

    var tokenTwo by rememberSaveable(stateSaver = TokenSaver) {
        mutableStateOf(Token(R.drawable.base_erc20, "Select Token"))
    }

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = stringResource(R.string.SwapToken_Title),
                navigationIcon = {
                    HsBackButton(onClick = onBackClick)
                },
                menuItems = listOf(
                    MenuItem(
                        title = TranslatableString.ResString(R.string.Settings_Title),
                        icon = R.drawable.ic_settings,
                        tint = ComposeAppTheme.colors.grey,
                        onClick = {
                            showDialog = true
                        }
                    )
                )
            )
        },
        bottomBar = {
            ButtonsGroupWithShade {
                ButtonPrimaryRed(
                    enabled = tokenOneValue.isNotBlank() && tokenTwoValue.isNotBlank(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp),
                    title = if (
                        tokenOneValue.isNotBlank() && tokenTwoValue.isNotBlank()
                    ) {
                        stringResource(R.string.proceed)
                    } else {
                        stringResource(R.string.swap_select_token)
                    },
                    onClick = {
                        navController.navigate("swapConfirmScreen")
                    }
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            VSpacer(12.dp)
            HeaderSorting(
                height = 32.dp
            ) {
                HSpacer(20.dp)
                body_leah(
                    text = stringResource(R.string.swap_sell),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.weight(1f))
                TokenSelectorRow(
                    tokenIcon = tokenOne.icon,
                    tokenName = tokenOne.name,
                    onClick = {
                        showDialog = true
                    }
                )
                HSpacer(20.dp)
            }
            FormsInput(
                modifier = Modifier.padding(horizontal = 16.dp),
                enabled = true,
                pasteEnabled = false,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                hint = "0.00",
                onValueChange = { newValue ->
                    tokenOneValue = newValue
                }
            )
            VSpacer(8.dp)

            HsIconButton(
                enabled = true,
                modifier = Modifier.size(42.dp),
                onClick = {
                    val tempToken = tokenOne
                    tokenOne = tokenTwo
                    tokenTwo = tempToken

                    val tempValue = tokenOneValue
                    tokenOneValue = tokenTwoValue
                    tokenTwoValue = tempValue
                },
                rippleColor = ComposeAppTheme.colors.grey50
            ) {
                Icon(
                    modifier = Modifier.size(32.dp),
                    painter = painterResource(R.drawable.ic_arrow_down_swap),
                    contentDescription = null,
                    tint = ComposeAppTheme.colors.grey
                )
            }
            VSpacer(2.dp)

            HeaderSorting(
                height = 32.dp
            ) {
                HSpacer(20.dp)
                body_leah(
                    text = stringResource(R.string.swap_buy),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.weight(1f))
                TokenSelectorRow(
                    tokenIcon = tokenTwo.icon,
                    tokenName = tokenTwo.name,
                    onClick = {
                        showDialog = true
                    }
                )
                HSpacer(20.dp)
            }

            FormsInput(
                modifier = Modifier.padding(horizontal = 16.dp),
                enabled = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                pasteEnabled = false,
                hint = "0.00",
                onValueChange = { newValue ->
                    tokenTwoValue = newValue
                }
            )
            VSpacer(2.dp)

            HeaderSorting(
                height = 28.dp
            ) {
                HSpacer(20.dp)
                subhead1_grey(
                    text = stringResource(R.string.swap_available_balance),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.weight(1f))
                subhead1_leah(
                    text = "2wETH",
                    maxLines = 1
                )
                HSpacer(20.dp)
            }
            VSpacer(32.dp)
        }
    }

//    if (showDialog) {
//        SwapSettingsDialog(
//            onCloseClick = { showDialog = false },
//            onCancelClick = { showDialog = false },
//        )
//    }
}

@Preview
@Composable
fun SelectBackupItemsScreenPreview() {
    ComposeAppTheme {
        SwapTokenScreen(
            navController = rememberNavController(),
            onBackClick = {}
        )
    }
}