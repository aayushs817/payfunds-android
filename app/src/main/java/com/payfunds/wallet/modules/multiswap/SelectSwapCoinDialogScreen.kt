package com.payfunds.wallet.modules.multiswap

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.horizontalsystems.marketkit.models.Blockchain
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.Coin
import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.marketkit.models.TokenType
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.alternativeImageUrl
import com.payfunds.wallet.core.badge
import com.payfunds.wallet.core.iconPlaceholder
import com.payfunds.wallet.core.imageUrl
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.AppBar
import com.payfunds.wallet.ui.compose.components.B2
import com.payfunds.wallet.ui.compose.components.Badge
import com.payfunds.wallet.ui.compose.components.D1
import com.payfunds.wallet.ui.compose.components.FormsInputSearch
import com.payfunds.wallet.ui.compose.components.HsIconButton
import com.payfunds.wallet.ui.compose.components.HsImage
import com.payfunds.wallet.ui.compose.components.MultitextM1
import com.payfunds.wallet.ui.compose.components.RowUniversal
import com.payfunds.wallet.ui.compose.components.SectionUniversalItem
import com.payfunds.wallet.ui.compose.components.VSpacer

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun SelectSwapCoinDialogScreen(
    title: String,
    coinBalanceItems: List<CoinBalanceItem>,
    onSearchTextChanged: (String) -> Unit,
    onClose: () -> Unit,
    onClickItem: (Token) -> Unit
) {
    Column(modifier = Modifier
        .statusBarsPadding()
        .background(color = ComposeAppTheme.colors.tyler)) {
        AppBar(
            title = title,
            navigationIcon = {
                HsIconButton(onClick = onClose) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_back),
                        contentDescription = "Close",
                        tint = ComposeAppTheme.colors.jacob
                    )
                }
            }
        )
        FormsInputSearch(
            modifier = Modifier.padding(horizontal = 12.dp),
            hint = stringResource(R.string.Swap_Search_Token),
            singleLine = true,
            startContent = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_search),
                    contentDescription = "Search Icon",
                    modifier = Modifier.size(20.dp),
                    tint = ComposeAppTheme.colors.grey
                )
            },
            onValueChange = { newValue ->
                onSearchTextChanged.invoke(newValue)
            }
        )
        VSpacer(12.dp)

        LazyColumn {
            items(coinBalanceItems) { coinItem ->
                SectionUniversalItem(borderTop = true) {
                    RowUniversal(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        onClick = {
                            onClickItem.invoke(coinItem.token)
                        }
                    ) {
                        HsImage(
                            url = coinItem.token.coin.imageUrl,
                            alternativeUrl = coinItem.token.coin.alternativeImageUrl,
                            placeholder = coinItem.token.iconPlaceholder,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.size(16.dp))
                        MultitextM1(
                            title = {
                                Row {
                                    B2(text = coinItem.token.coin.code)
                                    coinItem.token.badge?.let {
                                        Badge(text = it)
                                    }
                                }
                            },
                            subtitle = { D1(text = coinItem.token.coin.name) }
                        )
                        Spacer(modifier = Modifier.weight(1f))
                        MultitextM1(
                            title = {
                                coinItem.balance?.let {
                                    App.numberFormatter.formatCoinShort(
                                        it,
                                        coinItem.token.coin.code,
                                        8
                                    )
                                }?.let {
                                    B2(text = it)
                                }
                            },
                            subtitle = {
                                coinItem.fiatBalanceValue?.let { fiatBalanceValue ->
                                    App.numberFormatter.formatFiatShort(
                                        fiatBalanceValue.value,
                                        fiatBalanceValue.currency.symbol,
                                        2
                                    )
                                }?.let {
                                    D1(
                                        modifier = Modifier.align(Alignment.End),
                                        text = it
                                    )
                                }
                            }
                        )
                    }
                }
            }
            item {
                VSpacer(height = 32.dp)
            }
        }
    }
}


@Preview
@Composable
fun SelectSwapCoinDialogScreenPreview() {
    ComposeAppTheme {
        SelectSwapCoinDialogScreen(
            title = "Select Coin",
            coinBalanceItems = listOf(
                CoinBalanceItem(
                    token = Token(
                        coin = Coin("uid", "KuCoin", "KCS"),
                        blockchain = Blockchain(BlockchainType.Ethereum, "Ethereum", null),
                        type = TokenType.Eip20("eef"),
                        decimals = 18
                    ),
                    balance = null,
                    fiatBalanceValue = null
                )
            ),
            onSearchTextChanged = {},
            onClose = {},
            onClickItem = {}
        )
    }
}