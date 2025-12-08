package com.payfunds.wallet.modules.alert

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.getWalletAddress
import com.payfunds.wallet.modules.evmfee.ButtonsGroupWithShade
import com.payfunds.wallet.network.request_model.coinapi.CoinAPIRequestModel
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.AppBar
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.HSCircularProgressIndicator
import com.payfunds.wallet.ui.compose.components.HSpacer
import com.payfunds.wallet.ui.compose.components.HeaderSorting
import com.payfunds.wallet.ui.compose.components.HsBackButton
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.compose.components.caption_grey
import com.payfunds.wallet.ui.compose.components.caption_red50
import com.payfunds.wallet.ui.compose.components.headline2_leah
import io.payfunds.core.helpers.HudHelper


object AlertCreateScreen {
    var currentPrice = ""
    var frequencyList = ""
    var frequencySymbols = ""
    var tokenName = ""
    var tokenSymbol = ""
    var typeList = ""
    var typeSymbols = ""
}

@SuppressLint("DefaultLocale")
@Composable
fun AlertCreateScreen(
    onBackClick: () -> Unit,
) {
    val viewModelCoinAPI: CoinAPIViewModel = viewModel(factory = CoinAPIModule.Factory())
    val viewModel: AlertViewModel = viewModel(factory = AlertModule.Factory())
    var priceValue by remember { mutableStateOf("") }
    var walletAddress by remember { mutableStateOf("") }
    val errorMessage by viewModel.apiError
    val isLoading by viewModel.isApiLoading
    val view = LocalView.current
    var calculationDescription by remember { mutableStateOf("") }
    var calculationError by remember { mutableStateOf("") }
    var calculatedPrice by remember { mutableStateOf("") }

    val isLivePriceNull = viewModelCoinAPI.priceGet.value?.price == null
    val priceLive = viewModelCoinAPI.priceGet.value?.price?.let { price ->
        if (price > 1) {
            String.format("%.4f", price).trimEnd('0').trimEnd('.')
        } else {
            String.format("%.8f", price).trimEnd('0').trimEnd('.')
        }
    } ?: "Loading..."


    var frequencyList by remember { mutableStateOf(AlertCreateScreen.frequencyList) }
    var frequencySymbols by remember { mutableStateOf(AlertCreateScreen.frequencySymbols) }

    var typeList by remember { mutableStateOf(AlertCreateScreen.typeList) }
    var typeSymbols by remember { mutableStateOf(AlertCreateScreen.typeSymbols) }

    LaunchedEffect(Unit) {
        val account = App.accountManager.activeAccount
        account?.let {
            val address = getWalletAddress(it)
            walletAddress = address ?: ""
        }
    }

    LaunchedEffect(viewModel.alertCreateResponse.value) {
        viewModel.alertCreateResponse.value?.let {
            HudHelper.showSuccessMessage(view, "Alert has been created")
            viewModel.alertCreateResponse.value = null
        }
    }

    LaunchedEffect(Unit) {
        val requestModel = CoinAPIRequestModel(
            type = "subscribe",
            heartbeat = false,
            subscribe_data_type = listOf("trade"),
            subscribe_filter_exchange_id = listOf("BINANCE"),
            subscribe_update_limit_ms_quote = 1000,
            subscribe_filter_asset_id = listOf(AlertCreateScreen.tokenSymbol + "/USDT")
        )
        viewModelCoinAPI.sendCoinAPIRequest(requestModel)
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModelCoinAPI.disconnectWebSocket()
        }
    }
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = stringResource(R.string.alert_create_alert),
                navigationIcon = {
                    HsBackButton(onClick = { onBackClick() })
                },
                menuItems = listOf(

                )
            )
        },
        bottomBar = {
            ButtonsGroupWithShade {
                ButtonPrimaryRed(modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                    enabled = calculationError.isEmpty() && calculatedPrice.isNotEmpty(),
                    title = "Create Alert",
                    onClick = {
                        if (AlertCreateScreen.tokenSymbol == "USDT") {
                            HudHelper.showErrorMessage(
                                view,
                                "Something went wrong. Please select another token."
                            )
                        } else if (isLivePriceNull) {
                            HudHelper.showErrorMessage(
                                view,
                                "Something went wrong. Please select another token."
                            )
                        } else {
                            viewModel.alertCreate(
                                walletAddress = walletAddress,
                                frequency = frequencyList,
                                type = when (typeList) {
                                    AlertTypes.PRICE_REACHES.title -> {
                                        AlertTypes.PRICE_REACHES.value
                                    }

                                    AlertTypes.PRICE_RISES_ABOVE.title -> {
                                        AlertTypes.PRICE_RISES_ABOVE.value
                                    }

                                    AlertTypes.PRICE_DROP_TO.title -> {
                                        AlertTypes.PRICE_DROP_TO.value
                                    }

                                    AlertTypes.CHANGE_IS_OVER.title -> {
                                        AlertTypes.CHANGE_IS_OVER.value
                                    }

                                    AlertTypes.CHANGE_IS_UNDER.title -> {
                                        AlertTypes.CHANGE_IS_UNDER.value
                                    }

                                    else -> {
                                        AlertTypes.PRICE_REACHES.value
                                    }
                                },
                                tokenName = AlertCreateScreen.tokenName,
                                tokenSymbol = AlertCreateScreen.tokenSymbol,
                                price = calculatedPrice
                            )
                        }
                    })
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
        ) {
            VSpacer(12.dp)
            HeaderSorting(
                height = 28.dp
            ) {
                HSpacer(4.dp)
                headline2_leah(text = AlertCreateScreen.tokenSymbol)
                Spacer(modifier = Modifier.weight(1f))
                headline2_leah(text = if (isLivePriceNull) "Loading..." else "$$priceLive")
                HSpacer(4.dp)
            }
            VSpacer(24.dp)

            AlertTypeComponent(
                selectedAlert = (
                        AlertDropItem(
                            symbol = AlertCreateScreen.typeSymbols,
                            list = AlertCreateScreen.typeList
                        )),
                onAlertSelected = {
                    typeList = it.list
                    typeSymbols = it.symbol
                    priceValue = ""
                    calculationDescription = ""
                    calculationError = ""
                    calculatedPrice = ""
                }
            )
            VSpacer(24.dp)
            TextFieldAlertComponent(
                title = "Value",
                textFiledValue = priceValue,
                prefixText = if (typeSymbols == "%") "%" else "$",
                placeholder = if (typeSymbols == "%") "0.00%" else "$0",
                onTextChange = { newValue ->
                    priceValue = newValue
                    calculationDescription = ""
                    calculationError = ""
                    calculatedPrice = ""

                    if (priceValue.isNotEmpty() && !priceValue.startsWith(".") && !isLivePriceNull) {
                        calculatedPrice = priceValue

                        when (typeList) {

                            AlertTypes.PRICE_REACHES.title -> {

                                calculationDescription =
                                    "=${
                                        priceValue.toBigDecimal().toPlainString()
                                    } USDT (The alert wil be based on price of USDT)"

                                if (priceValue.toDouble() > (priceLive.replace(",", "")
                                        .toDouble() * 100)
                                ) {
                                    calculationError =
                                        "Alert price should not be more than 100 times the current price"
                                } else if (priceValue.toDouble() < (priceLive.replace(",", "")
                                        .toDouble() / 100)
                                ) {
                                    calculationError =
                                        "Alert price should not be less than 100 times the current price"
                                }

                            }

                            AlertTypes.PRICE_RISES_ABOVE.title -> {
                                calculationDescription =
                                    "=${
                                        priceValue.toBigDecimal().toPlainString()
                                    } USDT (The alert wil be based on price of USDT)"

                                if (priceValue.toDouble() <= priceLive.replace(",", "")
                                        .toDouble()
                                ) {
                                    calculationError =
                                        "Value should be larger than latest price"
                                } else if (priceValue.toDouble() > (priceLive.replace(",", "")
                                        .toDouble() * 100)
                                ) {
                                    calculationError =
                                        "Alert price should not be more than 100 times the current price"
                                }
                            }

                            AlertTypes.PRICE_DROP_TO.title -> {
                                calculationDescription =
                                    "=${
                                        priceValue.toBigDecimal().toPlainString()
                                    } USDT (The alert wil be based on price of USDT)"

                                if (priceValue.toDouble() > priceLive.replace(",", "")
                                        .toDouble()
                                ) {
                                    calculationError =
                                        "Value should be smaller than latest price"
                                } else if (priceValue.toDouble() < (priceLive.replace(",", "")
                                        .toDouble() / 100)
                                ) {
                                    calculationError =
                                        "Alert price should not be less than 100 times the current price"
                                }
                            }

                            AlertTypes.CHANGE_IS_OVER.title -> {
                                calculatedPrice = (
                                        priceLive.replace(",", "")
                                            .toDouble() + (priceLive.replace(
                                            ",",
                                            ""
                                        ).toDouble() / 100 * priceValue.toDouble())
                                        ).toString()
                                calculationDescription =
                                    "${calculatedPrice.toBigDecimal().toPlainString()} USDT"
                                if (priceValue.toDouble() > 1000) {
                                    priceValue = "1000"
                                }
                            }

                            AlertTypes.CHANGE_IS_UNDER.title -> {
                                calculatedPrice =
                                    (priceLive.replace(",", "").toDouble() - (priceLive.replace(
                                        ",",
                                        ""
                                    ).toDouble() / 100 * priceValue.toDouble())).toString()
                                calculationDescription =
                                    "${calculatedPrice.toBigDecimal().toPlainString()} USDT"
                                if (priceValue.toDouble() > 99) {
                                    priceValue = "99"
                                }
                            }
                        }


                    }
                },
                keyboardType = KeyboardType.Decimal
            )

            if (calculationDescription.isNotEmpty()) {
                VSpacer(8.dp)
                caption_grey(
                    modifier = Modifier.padding(start = 4.dp),
                    text = calculationDescription
                )
            }

            if (calculationError.isNotEmpty()) {
                VSpacer(4.dp)
                caption_red50(
                    modifier = Modifier.padding(start = 4.dp),
                    text = calculationError
                )
            }
            VSpacer(24.dp)
            AlertFrequencyComponent(
                selectedAlert = (
                        AlertDropItem(
                            symbol = AlertCreateScreen.frequencySymbols,
                            list = AlertCreateScreen.frequencyList
                        )),
                onAlertSelected = {
                    frequencyList = it.list
                    frequencySymbols = it.symbol
                }
            )
            VSpacer(32.dp)
        }

        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                HSCircularProgressIndicator()
            }
        }
        errorMessage?.let {
                HudHelper.showErrorMessage(view, it)
                viewModel.apiError.value = null
        }
    }
}

@Preview(
    showBackground = true,
    name = "Light mode",
    uiMode = 1,
    showSystemUi = true
)
@Composable
fun AlertCreatePreview() {
    ComposeAppTheme {
        AlertCreateScreen(
            onBackClick = {}
        )
    }
}

