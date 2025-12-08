package com.payfunds.wallet.modules.receive.ui

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.payfunds.wallet.R
import com.payfunds.wallet.core.alternativeImageUrl
import com.payfunds.wallet.core.iconPlaceholder
import com.payfunds.wallet.core.imageUrl
import com.payfunds.wallet.entities.Account
import com.payfunds.wallet.entities.Wallet
import com.payfunds.wallet.modules.receive.viewmodels.CoinForReceiveType
import com.payfunds.wallet.modules.receive.viewmodels.ReceiveTokenSelectViewModel
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.HsImage
import com.payfunds.wallet.ui.compose.components.RowUniversal
import com.payfunds.wallet.ui.compose.components.SearchBar
import com.payfunds.wallet.ui.compose.components.SectionUniversalItem
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.compose.components.body_leah
import com.payfunds.wallet.ui.compose.components.subhead2_grey
import kotlinx.coroutines.launch

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ReceiveTokenSelectScreen(
    activeAccount: Account,
    onMultipleAddressesClick: (String) -> Unit,
    onMultipleDerivationsClick: (String) -> Unit,
    onMultipleBlockchainsClick: (String) -> Unit,
    onCoinClick: (Wallet) -> Unit,
    onBackPress: () -> Unit,
) {
    val viewModel = viewModel<ReceiveTokenSelectViewModel>(
        factory = ReceiveTokenSelectViewModel.Factory(activeAccount)
    )
    val fullCoins = viewModel.uiState.fullCoins
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            SearchBar(
                title = stringResource(R.string.Balance_Receive),
                searchHintText = stringResource(R.string.Balance_ReceiveHint_Search),
                menuItems = listOf(),
                onClose = onBackPress,
                onSearchTextChanged = { text ->
                    viewModel.updateFilter(text)
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(contentPadding = paddingValues) {
            item {
                VSpacer(12.dp)
            }
            itemsIndexed(fullCoins) { index, fullCoin ->
                val coin = fullCoin.coin
                val lastItem = index == fullCoins.size - 1
                SectionUniversalItem(borderTop = true, borderBottom = lastItem) {
                    ReceiveCoin(
                        coinName = coin.name,
                        coinCode = coin.code,
                        coinIconUrl = coin.imageUrl,
                        alternativeCoinIconUrl = coin.alternativeImageUrl,
                        coinIconPlaceholder = fullCoin.iconPlaceholder,
                        onClick = {
                            coroutineScope.launch {
                                when (val coinActiveWalletsType =
                                    viewModel.getCoinForReceiveType(fullCoin)) {
                                    CoinForReceiveType.MultipleAddressTypes -> {
                                        onMultipleAddressesClick.invoke(coin.uid)
                                    }

                                    CoinForReceiveType.MultipleDerivations -> {
                                        onMultipleDerivationsClick.invoke(coin.uid)
                                    }

                                    CoinForReceiveType.MultipleBlockchains -> {
                                        onMultipleBlockchainsClick.invoke(coin.uid)
                                    }

                                    is CoinForReceiveType.Single -> {
                                        onCoinClick.invoke(coinActiveWalletsType.wallet)
                                    }

                                    null -> Unit
                                }
                            }
                        }
                    )
                }
            }
            item {
                VSpacer(32.dp)
            }
        }
    }
}

@Composable
fun ReceiveCoin(
    coinName: String,
    coinCode: String,
    coinIconUrl: String,
    alternativeCoinIconUrl: String?,
    coinIconPlaceholder: Int,
    onClick: (() -> Unit)? = null
) {
    RowUniversal(
        modifier = Modifier.padding(horizontal = 16.dp),
        onClick = onClick
    ) {
        HsImage(
            url = coinIconUrl,
            alternativeUrl = alternativeCoinIconUrl,
            placeholder = coinIconPlaceholder,
            modifier = Modifier
                .padding(end = 16.dp)
                .size(32.dp)
        )
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                body_leah(
                    modifier = Modifier
                        .weight(1f)
                        .padding(end = 16.dp),
                    text = coinCode,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            VSpacer(3.dp)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                subhead2_grey(
                    text = coinName,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
