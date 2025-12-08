package com.payfunds.wallet.modules.balance.ui

import android.view.View
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.slideFromBottom
import com.payfunds.wallet.modules.balance.BalanceViewItem2
import com.payfunds.wallet.modules.balance.BalanceViewModel
import com.payfunds.wallet.modules.syncerror.SyncErrorDialog
import com.payfunds.wallet.modules.walletconnect.list.ui.DraggableCardSimple
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.CellMultilineClear
import com.payfunds.wallet.ui.compose.components.CoinImage
import com.payfunds.wallet.ui.compose.components.HsIconButton
import com.payfunds.wallet.ui.compose.components.body_leah
import com.payfunds.wallet.ui.compose.components.diffColor
import com.payfunds.wallet.ui.compose.components.diffText
import com.payfunds.wallet.ui.compose.components.subhead2_grey
import com.payfunds.wallet.ui.extensions.RotatingCircleProgressView
import io.payfunds.core.helpers.HudHelper
import java.math.BigDecimal
import java.math.BigInteger

@Composable
fun BalanceCardSwipable(
    viewItem: BalanceViewItem2,
    revealed: Boolean,
    onReveal: (Int) -> Unit,
    onConceal: () -> Unit,
    onClick: () -> Unit,
    onClickSyncError: () -> Unit,
    onDisable: () -> Unit,
) {

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        HsIconButton(
            modifier = Modifier
                .fillMaxHeight()
                .align(Alignment.CenterEnd)
                .width(88.dp),
            onClick = onDisable,
            content = {
                Icon(
                    painter = painterResource(id = R.drawable.ic_circle_minus_24),
                    tint = Color.Gray,
                    contentDescription = "delete",
                )
            }
        )

        DraggableCardSimple(
            key = viewItem.wallet,
            isRevealed = revealed,
            cardOffset = 72f,
            onReveal = { onReveal(viewItem.wallet.hashCode()) },
            onConceal = onConceal,
            content = {
                BalanceCard(
                    onClick = onClick,
                    onClickSyncError = onClickSyncError,
                    viewItem = viewItem
                )
            }
        )
    }
}

@Composable
fun BalanceCard(
    onClick: () -> Unit,
    onClickSyncError: () -> Unit,
    viewItem: BalanceViewItem2
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
        BalanceCardInner(
            viewItem = viewItem,
            type = BalanceCardSubtitleType.Rate,
            onClickSyncError = onClickSyncError
        )
    }
}

enum class BalanceCardSubtitleType {
    Rate, CoinName
}

@Composable
fun BalanceCardInner(
    viewItem: BalanceViewItem2,
    type: BalanceCardSubtitleType,
    onClickSyncError: (() -> Unit)? = null
) {
    CellMultilineClear(height = 64.dp) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WalletIcon(viewItem, onClickSyncError)
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f),
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(weight = 1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        body_leah(
                            text = viewItem.wallet.coin.code,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (!viewItem.badge.isNullOrBlank()) {
                            Box(
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(ComposeAppTheme.colors.jeremy)
                            ) {
                                Text(
                                    modifier = Modifier.padding(
                                        start = 4.dp,
                                        end = 4.dp,
                                        bottom = 1.dp
                                    ),
                                    text = viewItem.badge,
                                    color = ComposeAppTheme.colors.bran,
                                    style = ComposeAppTheme.typography.microSB,
                                    maxLines = 1,
                                )
                            }
                        }
                    }
                    Spacer(Modifier.width(24.dp))

                    Text(
                        text = if (viewItem.primaryValue.visible) viewItem.primaryValue.value else "*****",
                        color = if (viewItem.primaryValue.dimmed) ComposeAppTheme.colors.grey else ComposeAppTheme.colors.leah,
                        style = ComposeAppTheme.typography.headline2,
                        maxLines = 1,
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Box(
                        modifier = Modifier.weight(1f),
                    ) {
                        if (viewItem.syncingTextValue != null) {
                            subhead2_grey(
                                text = viewItem.syncingTextValue,
                                maxLines = 1,
                            )
                        } else {
                            when (type) {
                                BalanceCardSubtitleType.Rate -> {
                                    if (viewItem.exchangeValue.visible) {
                                        Row {
                                            // $0.01111 only for EON (static data)
                                            Text(
                                                text = if (viewItem.wallet.token.coin.code.equals(
                                                        "EON",
                                                        true
                                                    )
                                                ) "$0.01111" else viewItem.exchangeValue.value,
                                                color = if (viewItem.exchangeValue.dimmed) ComposeAppTheme.colors.grey50 else ComposeAppTheme.colors.grey,
                                                style = ComposeAppTheme.typography.subhead2,
                                                maxLines = 1,
                                            )
                                            Text(
                                                modifier = Modifier.padding(start = 4.dp),
                                                text = diffText(viewItem.diff),
                                                color = diffColor(viewItem.diff),
                                                style = ComposeAppTheme.typography.subhead2,
                                                maxLines = 1,
                                            )
                                        }
                                    }
                                }

                                BalanceCardSubtitleType.CoinName -> {
                                    subhead2_grey(text = viewItem.wallet.coin.name)
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier.padding(start = 16.dp),
                    ) {

                        if (viewItem.syncedUntilTextValue != null) {
                            subhead2_grey(
                                text = viewItem.syncedUntilTextValue,
                                maxLines = 1,
                            )
                        } else {

                            val secondaryBalance = if (viewItem.wallet.token.coin.code.equals(
                                    "EON",
                                    true
                                )
                            ) "$" + BigDecimal("0.01111").multiply(BigDecimal(viewItem.primaryValue.value)).toString() else viewItem.secondaryValue.value

                            Text(
                                text = if (viewItem.secondaryValue.visible) secondaryBalance else "*****",
                                color = if (viewItem.secondaryValue.dimmed) ComposeAppTheme.colors.grey50 else ComposeAppTheme.colors.grey,
                                style = ComposeAppTheme.typography.subhead2,
                                maxLines = 1,
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(16.dp))
        }
    }
}

@Composable
private fun WalletIcon(
    viewItem: BalanceViewItem2,
    onClickSyncError: (() -> Unit)?
) {
    Box(
        modifier = Modifier
            .width(64.dp)
            .fillMaxHeight(),
        contentAlignment = Alignment.Center
    ) {
        viewItem.syncingProgress.progress?.let { progress ->
            AndroidView(
                modifier = Modifier
                    .size(52.dp),
                factory = { context ->
                    RotatingCircleProgressView(context)
                },
                update = { view ->
                    val color = when (viewItem.syncingProgress.dimmed) {
                        true -> R.color.grey_50
                        false -> R.color.grey
                    }
                    view.setProgressColored(progress, view.context.getColor(color))
                }
            )
        }
        if (viewItem.failedIconVisible) {
            val clickableModifier = if (onClickSyncError != null) {
                Modifier.clickable(onClick = onClickSyncError)
            } else {
                Modifier
            }

            Image(
                modifier = Modifier
                    .size(32.dp)
                    .then(clickableModifier),
                painter = painterResource(id = R.drawable.ic_attention_24),
                contentDescription = "coin icon",
                colorFilter = ColorFilter.tint(ComposeAppTheme.colors.lucian)
            )
        } else {

            CoinImage(
                token = viewItem.wallet.token,
                modifier = Modifier.size(32.dp)
            )
        }
    }
}

fun onSyncErrorClicked(
    viewItem: BalanceViewItem2,
    viewModel: BalanceViewModel,
    navController: NavController,
    view: View
) {
    when (val syncErrorDetails = viewModel.getSyncErrorDetails(viewItem)) {
        is BalanceViewModel.SyncError.Dialog -> {
            val wallet = syncErrorDetails.wallet
            val errorMessage = syncErrorDetails.errorMessage

            navController.slideFromBottom(
                R.id.syncErrorDialog,
                SyncErrorDialog.Input(wallet, errorMessage)
            )
        }

        is BalanceViewModel.SyncError.NetworkNotAvailable -> {
            HudHelper.showErrorMessage(view, R.string.Hud_Text_NoInternet)
        }
    }
}
