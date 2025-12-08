package com.payfunds.wallet.modules.alert

import android.annotation.SuppressLint
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material.DropdownMenu
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import com.payfunds.wallet.R
import com.payfunds.wallet.modules.evmfee.shadow
import com.payfunds.wallet.modules.walletconnect.list.ui.DraggableCardSimple
import com.payfunds.wallet.network.request_model.coinapi.CoinAPIRequestModel
import com.payfunds.wallet.network.response_model.alert.list.grouped.GroupAlert
import com.payfunds.wallet.network.response_model.alert.list.grouped.TokenAlert
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.AlertHeader
import com.payfunds.wallet.ui.compose.components.AlertItem
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.HSpacer
import com.payfunds.wallet.ui.compose.components.HsBackButton
import com.payfunds.wallet.ui.compose.components.HsCheckbox
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.compose.components.body_grey
import com.payfunds.wallet.ui.compose.components.body_leah
import com.payfunds.wallet.ui.compose.components.subhead1_grey
import com.payfunds.wallet.ui.compose.components.subhead1_leah
import com.payfunds.wallet.ui.compose.components.title3_leah

enum class AlertTypes(val title: String, val value: String) {
    PRICE_REACHES("Price reaches", "Price reaches"),
    PRICE_RISES_ABOVE("Price rises above", "Price above"),
    PRICE_DROP_TO("Price drops to", "Price below"),
    CHANGE_IS_OVER("Change is over", "Change is over"),
    CHANGE_IS_UNDER("Change is under", "Change is under"),
}

@Composable
fun TextFieldAlertComponent(
    title: String? = null,
    textFiledValue: String,
    prefixText: String,
    placeholder: String? = null,
    onTextChange: (String) -> Unit,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isFocused = interactionSource.collectIsFocusedAsState()
    val focusRequester = remember { FocusRequester() }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        subhead1_grey(text = title ?: "", modifier = Modifier.padding(start = 4.dp))
        VSpacer(8.dp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 44.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, ComposeAppTheme.colors.steel20, RoundedCornerShape(12.dp))
                .background(ComposeAppTheme.colors.lawrence)
                .clickable { focusRequester.requestFocus() },
            verticalAlignment = Alignment.CenterVertically
        ) {
            HSpacer(16.dp)
            if (textFiledValue.isEmpty() && !isFocused.value) {
                body_grey(
                    modifier = Modifier.clickable { focusRequester.requestFocus() },
                    text = placeholder ?: ""
                )
            } else {
                body_leah(
                    modifier = Modifier.clickable { focusRequester.requestFocus() },
                    text = "$prefixText "
                )
            }
            BasicTextField(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.CenterVertically)
                    .focusRequester(focusRequester),
                cursorBrush = SolidColor(ComposeAppTheme.colors.redG),
                maxLines = 1,
                interactionSource = interactionSource,
                value = textFiledValue,
                onValueChange = { onTextChange(it) },
                textStyle = ComposeAppTheme.typography.body.copy(
                    color = ComposeAppTheme.colors.leah
                ),
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            )
        }
    }
}


@Composable
fun AppBarAlert(
    onBackClick: () -> Unit,
    title: String,
    onEditClick: (() -> Unit)? = null,
    onInfoClick: (() -> Unit)? = null,
    onDoneClick: (() -> Unit)? = null,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(Modifier.width(4.dp))
            HsBackButton(onClick = onBackClick)
            Spacer(Modifier.width(16.dp))
            title3_leah(text = title)
        }
            Row(
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                onDoneClick?.let {
                    ButtonPrimaryRed(
                        modifier = Modifier.height(30.dp),
                        title = "Done",
                        onClick = onDoneClick
                    )
                }
                onEditClick?.let {
                    IconButton(
                        onClick = onEditClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_edit_24px),
                            contentDescription = "Edit",
                            tint = ComposeAppTheme.colors.redG,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                onInfoClick?.let {
                    IconButton(
                        onClick = onInfoClick
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_info_20),
                            contentDescription = "Info",
                            tint = ComposeAppTheme.colors.redG,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(Modifier.width(8.dp))
            }
        }
}


@Composable
fun AlertDropDownComponent(
    title: String? = null,
    listName: String? = null,
    listItems: List<AlertDropItem>,
    selectedItem: AlertDropItem,
    onItemSelected: (AlertDropItem) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf(selectedItem) }
    var textfieldSize by remember { mutableStateOf(Size.Zero) }

    val icon = if (expanded) R.drawable.ic_arrow_big_up_20
    else R.drawable.ic_arrow_big_down_20

    Column(
        Modifier.fillMaxWidth()
    ) {
        title?.let {
            subhead1_grey(
                modifier = Modifier.padding(start = 4.dp), text = title
            )
            VSpacer(8.dp)
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 44.dp)
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, ComposeAppTheme.colors.steel20, RoundedCornerShape(12.dp))
                .background(ComposeAppTheme.colors.lawrence)
                .clickable { expanded = !expanded }
                .onGloballyPositioned { coordinates ->
                    textfieldSize = coordinates.size.toSize()
                }
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.CenterStart,
        ) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Start
                ) {
                    body_leah(
                        text = selectedText.list
                    )
                }
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = "",
                    modifier = Modifier.size(16.dp), tint = ComposeAppTheme.colors.leah
                )
            }
        }
        MaterialTheme(
            shapes = MaterialTheme.shapes.copy(medium = RoundedCornerShape(16.dp))
        ) {
            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier
                    .background(ComposeAppTheme.colors.lawrence)
                    .width(with(LocalDensity.current) { textfieldSize.width.toDp() })
            ) {
                listName?.let {
                    AlertHeader(title = R.string.alert_type)
                }
                listItems.forEach { item ->
                    AlertItem(
                        onClick = {
                            selectedText = item
                            expanded = false
                            onItemSelected(item)
                        }) {
                        Text(
                            text = item.list,
                            color = if (item == selectedText) ComposeAppTheme.colors.redG
                            else ComposeAppTheme.colors.leah,
                            style = ComposeAppTheme.typography.body
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun AlertFrequencyComponent(
    selectedAlert: AlertDropItem,
    onAlertSelected: (AlertDropItem) -> Unit,
) {
    val alertOptions = listOf(
        AlertDropItem(symbol = "$", list = "Only once"),
        AlertDropItem(symbol = "%", list = "Once a day"),
        AlertDropItem(symbol = "%", list = "Always"),
    )
    AlertDropDownComponent(
        listName = "Frequency",
        title = "Frequency",
        listItems = alertOptions,
        selectedItem = selectedAlert,
        onItemSelected = onAlertSelected
    )
}

@Composable
fun AlertTypeComponent(
    selectedAlert: AlertDropItem, onAlertSelected: (AlertDropItem) -> Unit,
) {
    val alertOptions = listOf(
        AlertDropItem(symbol = "$", list = AlertTypes.PRICE_REACHES.title),
        AlertDropItem(symbol = "$", list = AlertTypes.PRICE_RISES_ABOVE.title),
        AlertDropItem(symbol = "$", list = AlertTypes.PRICE_DROP_TO.title),
        AlertDropItem(symbol = "%", list = AlertTypes.CHANGE_IS_OVER.title),
        AlertDropItem(symbol = "%", list = AlertTypes.CHANGE_IS_UNDER.title),
    )
    AlertDropDownComponent(
        listName = "Alert Type",
        title = "Alert Type",
        listItems = alertOptions,
        selectedItem = selectedAlert,
        onItemSelected = onAlertSelected
    )
}



@Composable
fun ItemSelectAllComponent(
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    isDeleteEnabled: Boolean,
    onDeleteClick: () -> Unit
) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row {
            HsCheckbox(
                checked = isChecked,
                enabled = true,
                onCheckedChange = { checked ->
                    onCheckedChange(checked)
                },
            )
            HSpacer(6.dp)
            subhead1_leah(text = stringResource(R.string.alert_select_all))
        }
        ButtonPrimaryRed(
            enabled = isDeleteEnabled,
            title = stringResource(R.string.alert_delete),
            onClick = {
                onDeleteClick()
            }
        )
    }
}


@Composable
fun ItemSwapToDeleteComponent(
    alert: GroupAlert
) {
    CustomBoxComponent(
        modifier = Modifier.fillMaxWidth(), boxHeight = 78.dp
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                val iconRes = when (alert.type) {
                    AlertTypes.PRICE_REACHES.value -> R.drawable.ic_arrow_right_20
                    AlertTypes.PRICE_RISES_ABOVE.value -> R.drawable.ic_arrow_up_20
                    AlertTypes.PRICE_DROP_TO.value -> R.drawable.ic_arrow_down_20
                    AlertTypes.CHANGE_IS_OVER.value -> R.drawable.ic_arrow_up_20
                    AlertTypes.CHANGE_IS_UNDER.value -> R.drawable.ic_arrow_down_20
                    else -> R.drawable.ic_arrow_up_20
                }

                val iconTint = when (alert.type) {
                    AlertTypes.PRICE_REACHES.value -> ComposeAppTheme.colors.grey
                    AlertTypes.PRICE_RISES_ABOVE.value -> ComposeAppTheme.colors.greenL
                    AlertTypes.PRICE_DROP_TO.value -> ComposeAppTheme.colors.redL
                    AlertTypes.CHANGE_IS_OVER.value -> ComposeAppTheme.colors.greenL
                    AlertTypes.CHANGE_IS_UNDER.value -> ComposeAppTheme.colors.redL
                    else -> ComposeAppTheme.colors.grey
                }

                val alertText = when (alert.type) {
                    AlertTypes.PRICE_REACHES.value -> "${AlertTypes.PRICE_REACHES.title} ${
                        alert.price.replace(
                            ",",
                            ""
                        ).toBigDecimal().toPlainString()
                    } USDT"
                    AlertTypes.PRICE_RISES_ABOVE.value,
                    AlertTypes.CHANGE_IS_OVER.value -> "${AlertTypes.PRICE_RISES_ABOVE.title} ${
                        alert.price.replace(
                            ",",
                            ""
                        ).toBigDecimal().toPlainString()
                    } USDT"

                    AlertTypes.PRICE_DROP_TO.value,
                    AlertTypes.CHANGE_IS_UNDER.value -> "${AlertTypes.PRICE_DROP_TO.title} ${
                        alert.price.replace(
                            ",",
                            ""
                        ).toBigDecimal().toPlainString()
                    } USDT"
                    else -> alert.type
                }

                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = alert.type,
                    tint = iconTint,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(4.dp))
                subhead1_leah(text = alertText)
            }
            Spacer(Modifier.height(4.dp))
            Row {
                Spacer(Modifier.width(20.dp))
                Text(
                    modifier = Modifier
                        .background(
                            ComposeAppTheme.colors.redG.copy(alpha = 0.2f), RoundedCornerShape(4.dp)
                        )
                        .padding(4.dp),
                    text = alert.frequency, color = ComposeAppTheme.colors.redG,
                    fontSize = 14.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.W400, fontFamily = FontFamily(Font(R.font.sora_font))
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    modifier = Modifier
                        .background(
                            ComposeAppTheme.colors.grey.copy(alpha = 0.2f), RoundedCornerShape(4.dp)
                        )
                        .padding(4.dp),
                    text = "Last Price",
                    color = ComposeAppTheme.colors.grey,
                    fontSize = 14.sp,
                    lineHeight = 17.sp,
                    fontWeight = FontWeight.W400, fontFamily = FontFamily(Font(R.font.sora_font))
                )
            }
        }

    }
}


@Composable
fun CustomBoxComponent(
    modifier: Modifier = Modifier,
    boxHeight: Dp = 60.dp,
    radius: Dp = 12.dp,
    onClick: () -> Unit = {},
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                blurRadius = radius,
                offsetY = 0.dp,
                offsetX = 0.dp,
                spread = 0f.dp,
                color = Color(
                    Color.Black
                        .copy(alpha = 0.06F)
                        .toArgb()
                ),
                borderRadius = radius,
            )
            .clip(RoundedCornerShape(radius))
            .background(ComposeAppTheme.colors.lawrence)
            .height(boxHeight)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
    ) {
        content()
    }
}

@SuppressLint("DefaultLocale")
@Composable
fun ItemAllComponent(
    viewModelCoinAPI: CoinAPIViewModel,
    alert: TokenAlert,
    isEditMode: Boolean,
    selectedAlertIds: List<String>,
    onSelectionChange: (Boolean, List<String>) -> Unit,
    onDeleteClick: (String) -> Unit,
) {

    var isExpanded by remember { mutableStateOf(false) }
    var revealedCardId by remember { mutableStateOf<Int?>(null) }

    val requestModel = CoinAPIRequestModel(
        type = "subscribe",
        heartbeat = false,
        subscribe_data_type = listOf("trade"),
        subscribe_filter_exchange_id = listOf("BINANCE"),
        subscribe_update_limit_ms_quote = 1000,
        subscribe_filter_asset_id = listOf("${alert.symbol}/USDT")
    )
    val coinAPIState by viewModelCoinAPI.getStateForToken(alert.symbol).collectAsState()

    LaunchedEffect(alert.symbol) {
        viewModelCoinAPI.sendCoinAPIRequests(requestModel)
    }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .height(72.dp)
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = {
                    if (coinAPIState?.price != null) {
                        isExpanded = !isExpanded
                    }
                }),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(16.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (isEditMode) {
                    Row {
                        HsCheckbox(
                            checked = selectedAlertIds.containsAll(alert.alerts.map { groupAlert -> groupAlert._id }),
                            enabled = true,
                            onCheckedChange = {
                                onSelectionChange(
                                    it, alert.alerts.map { groupAlert -> groupAlert._id })
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()) {
                    Row {
                        body_leah(text = alert.symbol)
                        body_grey(text = "/USDT")
                    }
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            body_leah(
                                text = coinAPIState?.price?.toBigDecimal()?.toPlainString()?.let {
                                    "$$it"
                                } ?: "Loading..."
                            )
                            HSpacer(4.dp)
                            Icon(
                                painter = painterResource(
                                    id = if (isExpanded) R.drawable.ic_arrow_big_down_20
                                    else R.drawable.ic_arrow_big_up_20
                                ),
                                contentDescription = null,
                                tint = ComposeAppTheme.colors.grey,
                                modifier = Modifier
                                    .size(16.dp)
                                    .align(Alignment.CenterVertically)
                            )
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        if (isExpanded) {
            Column {
                alert.alerts.forEach { itemAlert ->
                    ItemAllExpandList(
                        alert = itemAlert,
                        isEditMode = isEditMode,
                        isSelected = selectedAlertIds.contains(itemAlert._id),
                        revealed = revealedCardId == itemAlert._id.hashCode(),
                        onReveal = { idHashCode ->
                            if (revealedCardId != idHashCode) {
                                revealedCardId = idHashCode
                            }
                        },
                        onConceal = {
                            revealedCardId = null
                        },
                        onSelectionChange = { isSelected, id ->
                            onSelectionChange(isSelected, listOf(id))
                        },
                        onDeleteClick = onDeleteClick
                    )
                }
            }
        }
        Divider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = ComposeAppTheme.colors.steel10,
        )
    }
}

@Composable
fun ItemAllExpandList(
    alert: GroupAlert,
    isEditMode: Boolean,
    isSelected: Boolean,
    revealed: Boolean,
    onReveal: (Int) -> Unit,
    onConceal: () -> Unit,
    onSelectionChange: (Boolean, String) -> Unit,
    onDeleteClick: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isEditMode) {
            Row {
                HsCheckbox(
                    checked = isSelected,
                    enabled = true,
                    onCheckedChange = {
                        onSelectionChange(it, alert._id)
                    }
                )
                Spacer(Modifier.width(12.dp))
            }
        }
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            if (revealed) {
                Box(
                    modifier = Modifier
                        .height(78.dp)
                        .width(78.dp)
                        .align(Alignment.CenterEnd)
                        .clip(RoundedCornerShape(4.dp))
                        .background(ComposeAppTheme.colors.redG)
                        .clickable {
                            onDeleteClick(alert._id)
                        },
                    contentAlignment = Alignment.Center
                )
                {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_delete_20),
                        tint = Color.White,
                        contentDescription = "delete",
                    )
                }
            }

            DraggableCardSimple(
                key = alert._id,
                isRevealed = revealed,
                cardOffset = 100f,
                onReveal = { onReveal(alert._id.hashCode()) },
                onConceal = onConceal,
                content = {
                    ItemSwapToDeleteComponent(alert = alert)
                })
        }
    }
    Spacer(Modifier.height(16.dp))
}


@Composable
fun AlertEmptyComponent() {
    val isLight: Boolean = !isSystemInDarkTheme()
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = if (isLight) painterResource(id = R.drawable.ic_empty_alert_light)
                else painterResource(id = R.drawable.ic_empty_alert_dark),
                contentDescription = stringResource(R.string.alert_empty),
                modifier = Modifier.fillMaxWidth(),
                contentScale = ContentScale.FillWidth
            )
        }
        Spacer(Modifier.height(16.dp))
        body_grey(
            text = stringResource(R.string.alert_empty),
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )
    }
}

@Preview
@Composable
fun AlertErrorComponent() {
    ComposeAppTheme {
        ItemAllComponent(
            viewModelCoinAPI = CoinAPIViewModel(),
            alert = TokenAlert(
                symbol = "ETH",
                alerts = listOf(),
                _id = "",
                name = ""

            ),
            isEditMode = false,
            selectedAlertIds = listOf(),
            onSelectionChange = { _, _ -> },
            onDeleteClick = { }


        )
    }
}
