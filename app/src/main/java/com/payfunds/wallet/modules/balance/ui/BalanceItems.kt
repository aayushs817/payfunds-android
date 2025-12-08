package com.payfunds.wallet.modules.balance.ui

import android.content.Intent
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.DashboardObject
import com.payfunds.wallet.core.managers.FaqManager
import com.payfunds.wallet.core.providers.Translator
import com.payfunds.wallet.core.slideFromBottom
import com.payfunds.wallet.core.slideFromRight
import com.payfunds.wallet.core.stats.StatEvent
import com.payfunds.wallet.core.stats.StatPage
import com.payfunds.wallet.core.stats.stat
import com.payfunds.wallet.modules.balance.AccountViewItem
import com.payfunds.wallet.modules.balance.BalanceSortType
import com.payfunds.wallet.modules.balance.BalanceUiState
import com.payfunds.wallet.modules.balance.BalanceViewItem2
import com.payfunds.wallet.modules.balance.BalanceViewModel
import com.payfunds.wallet.modules.balance.HeaderNote
import com.payfunds.wallet.modules.balance.ReceiveAllowedState
import com.payfunds.wallet.modules.balance.TotalUIState
import com.payfunds.wallet.modules.coin.overview.ui.Loading
import com.payfunds.wallet.modules.manageaccount.dialogs.BackupRequiredDialog
import com.payfunds.wallet.modules.rateapp.RateAppModule
import com.payfunds.wallet.modules.rateapp.RateAppViewModel
import com.payfunds.wallet.modules.sendtokenselect.SendTokenSelectFragment
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.HSSwipeRefresh
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryCircle
import com.payfunds.wallet.ui.compose.components.ButtonSecondaryCircle
import com.payfunds.wallet.ui.compose.components.ButtonSecondaryTransparent
import com.payfunds.wallet.ui.compose.components.CellMultilineClear
import com.payfunds.wallet.ui.compose.components.DoubleText
import com.payfunds.wallet.ui.compose.components.HSpacer
import com.payfunds.wallet.ui.compose.components.HeaderSorting
import com.payfunds.wallet.ui.compose.components.HsIconButton
import com.payfunds.wallet.ui.compose.components.SelectorDialogCompose
import com.payfunds.wallet.ui.compose.components.SelectorItem
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.compose.components.body_leah
import com.payfunds.wallet.ui.compose.components.caption_leah
import com.payfunds.wallet.ui.compose.components.subhead2_grey
import com.payfunds.wallet.ui.compose.components.subhead2_leah
import io.payfunds.core.helpers.HudHelper

@Composable
fun NoteWarning(
    modifier: Modifier = Modifier,
    text: String,
    onClick: (() -> Unit),
    onClose: (() -> Unit)?
) {
    Note(
        modifier = modifier.clickable(onClick = onClick),
        text = text,
        title = stringResource(id = R.string.AccountRecovery_Note),
        icon = R.drawable.ic_attention_20,
        borderColor = ComposeAppTheme.colors.jacob,
        backgroundColor = ComposeAppTheme.colors.yellow20,
        textColor = ComposeAppTheme.colors.jacob,
        iconColor = ComposeAppTheme.colors.jacob,
        onClose = onClose
    )
}

@Composable
fun NoteError(
    modifier: Modifier = Modifier,
    text: String,
    onClick: (() -> Unit)
) {
    Note(
        modifier = modifier.clickable(onClick = onClick),
        text = text,
        title = stringResource(id = R.string.AccountRecovery_Note),
        icon = R.drawable.ic_attention_20,
        borderColor = ComposeAppTheme.colors.lucian,
        backgroundColor = ComposeAppTheme.colors.red20,
        textColor = ComposeAppTheme.colors.lucian,
        iconColor = ComposeAppTheme.colors.lucian
    )
}

@Composable
fun Note(
    modifier: Modifier = Modifier,
    text: String,
    title: String,
    @DrawableRes icon: Int,
    iconColor: Color,
    borderColor: Color,
    backgroundColor: Color,
    textColor: Color,
    onClose: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = iconColor
            )
            Text(
                modifier = Modifier.weight(1f),
                text = title,
                color = textColor,
                style = ComposeAppTheme.typography.subhead1
            )
            onClose?.let {
                HsIconButton(
                    modifier = Modifier.size(20.dp),
                    onClick = onClose
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        tint = iconColor,
                        contentDescription = null,
                    )
                }
            }
        }
        if (text.isNotEmpty()) {
            subhead2_leah(text = text)
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun BalanceItems(
    balanceViewItems: List<BalanceViewItem2>,
    viewModel: BalanceViewModel,
    accountViewItem: AccountViewItem,
    navController: NavController,
    uiState: BalanceUiState,
    totalState: TotalUIState
) {
    val rateAppViewModel = viewModel<RateAppViewModel>(factory = RateAppModule.Factory())
    DisposableEffect(true) {
        rateAppViewModel.onBalancePageActive()
        onDispose {
            rateAppViewModel.onBalancePageInactive()
        }
    }

    val context = LocalContext.current
    val view = LocalView.current
    var revealedCardId by remember { mutableStateOf<Int?>(null) }

    val navigateToTokenBalance: (BalanceViewItem2) -> Unit = remember {
        {
            navController.slideFromRight(
                R.id.tokenBalanceFragment,
                it.wallet
            )
            stat(page = StatPage.Balance, event = StatEvent.OpenTokenPage(it.wallet.token))
        }
    }

    val onClickSyncError: (BalanceViewItem2) -> Unit = remember {
        {
            onSyncErrorClicked(
                it,
                viewModel,
                navController,
                view
            )
        }
    }

    val onDisable: (BalanceViewItem2) -> Unit = remember {
        {
            viewModel.disable(it)
        }
    }

    HSSwipeRefresh(
        refreshing = uiState.isRefreshing,
        onRefresh = viewModel::onRefresh
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = rememberSaveable(
                    accountViewItem.id,
                    uiState.sortType,
                    saver = LazyListState.Saver
                ) {
                    LazyListState()
                }
            ) {
                item {
                    TotalBalanceRow(
                        totalState = totalState,
                        onClickTitle = remember {
                            {
                                viewModel.toggleBalanceVisibility()
                                HudHelper.vibrate(context)

                                stat(page = StatPage.Balance, event = StatEvent.ToggleBalanceHidden)
                            }
                        },
                        onClickSubtitle = remember {
                            {
                                viewModel.toggleTotalType()
                                HudHelper.vibrate(context)

                                stat(
                                    page = StatPage.Balance,
                                    event = StatEvent.ToggleConversionCoin
                                )
                            }
                        }
                    )
                }

                if (uiState.balanceTabButtonsEnabled && !accountViewItem.isWatchAccount) {
                    item {
                        Row(
                            modifier = Modifier
                                .padding(horizontal = 24.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {

                            ButtonPrimaryCircleRed(
                                icon = R.drawable.ic_wallet_receive,
                                title = "RECEIVE",
                                onClick = {
                                    when (val receiveAllowedState =
                                        viewModel.getReceiveAllowedState()) {
                                        ReceiveAllowedState.Allowed -> {
                                            navController.slideFromRight(R.id.receiveFragment)

                                            stat(
                                                page = StatPage.Balance,
                                                event = StatEvent.Open(StatPage.ReceiveTokenList)
                                            )
                                        }

                                        is ReceiveAllowedState.BackupRequired -> {
                                            val account = receiveAllowedState.account
                                            val text = Translator.getString(
                                                R.string.Balance_Receive_BackupRequired_Description,
                                                account.name
                                            )
                                            navController.slideFromBottom(
                                                R.id.backupRequiredDialog,
                                                BackupRequiredDialog.Input(account, text)
                                            )

                                            stat(
                                                page = StatPage.Balance,
                                                event = StatEvent.Open(StatPage.BackupRequired)
                                            )
                                        }

                                        null -> Unit
                                    }
                                }
                            )
                            HSpacer(8.dp)

                            ButtonPrimaryCircleRed(
                                icon = R.drawable.ic_wallet_send,
                                title = "SEND",
                                onClick = {
                                    navController.slideFromRight(R.id.sendTokenSelectFragment)

                                    stat(
                                        page = StatPage.Balance,
                                        event = StatEvent.Open(StatPage.SendTokenList)
                                    )
                                }
                            )

                            if (viewModel.isSwapEnabled) {
                                HSpacer(8.dp)
                                ButtonPrimaryCircleRed(
                                    title = "SWAP",
                                    icon = R.drawable.ic_swap_24,
                                    onClick = {
                                        navController.slideFromRight(R.id.multiswap)

                                        stat(
                                            page = StatPage.Balance,
                                            event = StatEvent.Open(StatPage.Swap)
                                        )
                                    }
                                )
                            }
//                            HSpacer(8.dp)
//                            ButtonPrimaryCircleRed(
//                                title = "BUY",
//                                icon = R.drawable.ic_wallet_buy,
//                                onClick = {
//                                    when (val receiveAllowedState =
//                                        viewModel.getReceiveAllowedState()) {
//                                        ReceiveAllowedState.Allowed -> {
//                                            navController.slideFromRight(
//                                                R.id.multiswap,
//                                                SwapFragment.Input(Action.BUY)
//                                            )
//                                            stat(
//                                                page = StatPage.Balance,
//                                                event = StatEvent.Open(StatPage.ReceiveTokenList)
//                                            )
//                                        }
//
//                                        is ReceiveAllowedState.BackupRequired -> {
//                                            val account = receiveAllowedState.account
//                                            val text = Translator.getString(
//                                                R.string.Balance_Receive_BackupRequired_Description,
//                                                account.name
//                                            )
//                                            navController.slideFromBottom(
//                                                R.id.backupRequiredDialog,
//                                                BackupRequiredDialog.Input(account, text)
//                                            )
//
//                                            stat(
//                                                page = StatPage.Balance,
//                                                event = StatEvent.Open(StatPage.BackupRequired)
//                                            )
//                                        }
//
//                                        null -> Unit
//                                    }
//                                }
//                            )
//
//                            HSpacer(8.dp)
//                            ButtonPrimaryCircleRed(
//                                title = "SELL",
//                                icon = R.drawable.ic_wallet_sell,
//                                onClick = {
//                                    when (val receiveAllowedState =
//                                        viewModel.getReceiveAllowedState()) {
//                                        ReceiveAllowedState.Allowed -> {
//                                            navController.slideFromRight(
//                                                R.id.multiswap,
//                                                SwapFragment.Input(Action.SELL)
//                                            )
//                                            stat(
//                                                page = StatPage.Balance,
//                                                event = StatEvent.Open(StatPage.ReceiveTokenList)
//                                            )
//                                        }
//
//                                        is ReceiveAllowedState.BackupRequired -> {
//                                            val account = receiveAllowedState.account
//                                            val text = Translator.getString(
//                                                R.string.Balance_Receive_BackupRequired_Description,
//                                                account.name
//                                            )
//                                            navController.slideFromBottom(
//                                                R.id.backupRequiredDialog,
//                                                BackupRequiredDialog.Input(account, text)
//                                            )
//
//                                            stat(
//                                                page = StatPage.Balance,
//                                                event = StatEvent.Open(StatPage.BackupRequired)
//                                            )
//                                        }
//
//                                        null -> Unit
//                                    }
//                                }
//                            )
                        }
                        VSpacer(12.dp)
                    }
                }

                item {
                    Divider(
                        thickness = 1.dp,
                        color = ComposeAppTheme.colors.steel10,
                    )
                }

                stickyHeader {
                    HeaderSorting {
                        BalanceSortingSelector(
                            sortType = uiState.sortType,
                            sortTypes = uiState.sortTypes
                        ) {
                            viewModel.setSortType(it)
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        if (accountViewItem.isWatchAccount) {
                            Image(
                                painter = painterResource(R.drawable.icon_binocule_24),
                                contentDescription = "binoculars icon"
                            )
                            HSpacer(16.dp)
                        }

                        ButtonSecondaryCircle(
                            icon = R.drawable.ic_manage_2,
                            contentDescription = stringResource(R.string.ManageCoins_title),
                            onClick = {
                                navController.slideFromRight(R.id.manageWalletsFragment)

                                stat(
                                    page = StatPage.Balance,
                                    event = StatEvent.Open(StatPage.CoinManager)
                                )
                            }
                        )

                        HSpacer(16.dp)
                    }
                }

                item {
                    when (uiState.headerNote) {
                        HeaderNote.None -> Unit
                        HeaderNote.NonStandardAccount -> {
                            NoteError(
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 12.dp,
                                    bottom = 24.dp
                                ),
                                text = stringResource(R.string.AccountRecovery_MigrationRequired),
                                onClick = {
                                    FaqManager.showFaqPage(
                                        navController,
                                        FaqManager.faqPathMigrationRequired
                                    )
                                }
                            )
                        }

                        HeaderNote.NonRecommendedAccount -> {
                            NoteWarning(
                                modifier = Modifier.padding(
                                    start = 16.dp,
                                    end = 16.dp,
                                    top = 12.dp,
                                    bottom = 24.dp
                                ),
                                text = stringResource(R.string.AccountRecovery_MigrationRecommended),
                                onClick = {
                                    FaqManager.showFaqPage(
                                        navController,
                                        FaqManager.faqPathMigrationRecommended
                                    )
                                },
                                onClose = {
                                    viewModel.onCloseHeaderNote(HeaderNote.NonRecommendedAccount)
                                }
                            )
                        }
                    }
                }

                item {
                    StoneFortCard {
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            "https://partners-stonefortsecurities.com/visit/?bta=35333&brand=stonefort&campaign=VC".toUri()
                        )
                        context.startActivity(browserIntent)
                    }
                }

                item {

                    VSpacer(8.dp)

                    BookBalanceCard {
                        val browserIntent = Intent(
                            Intent.ACTION_VIEW,
                            "https://cryptosellbuy.com/signup".toUri()
                        )
                        context.startActivity(browserIntent)
                    }
                }

                if (balanceViewItems.isEmpty()) {
                    item {
                        NoCoinsBlock()
                    }
                } else {

                    wallets(
                        items = balanceViewItems,
                        key = {
                            it.wallet.hashCode()
                        }
                    ) { item ->
                        BalanceCardSwipable(
                            viewItem = item,
                            revealed = revealedCardId == item.wallet.hashCode(),
                            onReveal = { walletHashCode ->
                                if (revealedCardId != walletHashCode) {
                                    revealedCardId = walletHashCode
                                }
                            },
                            onConceal = {
                                revealedCardId = null
                            },
                            onClick = {
                                navigateToTokenBalance.invoke(item)
                            },
                            onClickSyncError = {
                                onClickSyncError.invoke(item)
                            },
                            onDisable = {
                                onDisable.invoke(item)
                            }
                        )
                    }
                }
            }

            if (uiState.isLoading) {
                Loading()
            }
        }
    }
    uiState.openSend?.let { openSend ->
        navController.slideFromRight(
            R.id.sendTokenSelectFragment,
            SendTokenSelectFragment.Input(
                openSend.blockchainTypes,
                openSend.tokenTypes,
                openSend.address,
                openSend.amount
            )
        )
        viewModel.onSendOpened()
    }
}

@Composable
private fun BookBalanceCard(
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ComposeAppTheme.colors.lawrence)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        CellMultilineClear(height = 64.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.book_icon),
                        contentDescription = "",
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    body_leah(
                        text = "Reader",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    VSpacer(2.dp)
                    subhead2_grey(
                        text = "Enrich your knowledge",
                        maxLines = 1,
                    )
                }
            }
        }
    }
}

@Composable
private fun StoneFortCard(
    onClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
    ) {
        CellMultilineClear(height = 64.dp) {
            Row(verticalAlignment = Alignment.CenterVertically) {

                Box(
                    modifier = Modifier
                        .width(64.dp)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.stone_fort),
                        contentDescription = "",
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    body_leah(
                        text = "stonefort",
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        color = Color.White
                    )


                    VSpacer(2.dp)
                    subhead2_grey(
                        text = "Open your trading account",
                        maxLines = 1,
                        color = Color(0xFF8B9075)
                    )
                }
            }
        }
    }
}

@Composable
private fun NoCoinsBlock() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        VSpacer(height = 100.dp)
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    color = ComposeAppTheme.colors.raina,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                modifier = Modifier.size(48.dp),
                painter = painterResource(R.drawable.ic_empty_wallet),
                contentDescription = null,
                tint = ComposeAppTheme.colors.grey
            )
        }
        VSpacer(32.dp)
        subhead2_grey(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = stringResource(R.string.Balance_NoCoinsAlert),
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis,
        )
        VSpacer(height = 32.dp)
    }
}

@Composable
fun BalanceSortingSelector(
    sortType: BalanceSortType,
    sortTypes: List<BalanceSortType>,
    onSelectSortType: (BalanceSortType) -> Unit
) {
    var showSortTypeSelectorDialog by remember { mutableStateOf(false) }

    ButtonSecondaryTransparent(
        title = stringResource(sortType.getTitleRes()),
        iconRight = R.drawable.ic_down_arrow_20,
        onClick = {
            showSortTypeSelectorDialog = true
        }
    )

    if (showSortTypeSelectorDialog) {
        SelectorDialogCompose(
            title = stringResource(R.string.Balance_Sort_PopupTitle),
            items = sortTypes.map {
                SelectorItem(stringResource(it.getTitleRes()), it == sortType, it)
            },
            onDismissRequest = {
                showSortTypeSelectorDialog = false
            },
            onSelectItem = onSelectSortType
        )
    }
}

@Composable
fun TotalBalanceRow(
    totalState: TotalUIState,
    onClickTitle: () -> Unit,
    onClickSubtitle: () -> Unit
) {
    when (totalState) {
        TotalUIState.Hidden -> {
            DoubleText(
                title = "*****",
                body = "*****",
                dimmed = false,
                onClickTitle = onClickTitle,
                onClickSubtitle = onClickSubtitle
            )
        }

        is TotalUIState.Visible -> {
            DoubleText(
                title = totalState.primaryAmountStr,
                body = totalState.secondaryAmountStr,
                dimmed = totalState.dimmed,
                onClickTitle = onClickTitle,
                onClickSubtitle = onClickSubtitle,
            )
        }
    }
}

fun <T> LazyListScope.wallets(
    items: List<T>,
    key: ((item: T) -> Any)? = null,
    itemContent: @Composable (LazyItemScope.(item: T) -> Unit),
) {
    item {
        VSpacer(height = 8.dp)
    }
    items(items = items, key = key, itemContent = {
        Row(modifier = Modifier.padding(bottom = 8.dp)) {
            itemContent(it)
        }
    })
    item {
        VSpacer(height = 10.dp)
    }
}


@Composable
fun ButtonPrimaryCircleRed(
    title: String,
    @DrawableRes icon: Int = R.drawable.ic_arrow_down_left_24,
    onClick: () -> Unit,
    enabled: Boolean = true
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val shape = CircleShape
        HsIconButton(
            onClick = { onClick() },
            modifier = Modifier
                .size(50.dp)
                .clip(shape)
                .background(ComposeAppTheme.colors.redG),
            enabled = enabled,
            rippleColor = ComposeAppTheme.colors.claude
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(id = icon),
                contentDescription = title,
                tint = ComposeAppTheme.colors.white
            )
        }
        VSpacer(12.dp)
        caption_leah(text = title)
    }
}

