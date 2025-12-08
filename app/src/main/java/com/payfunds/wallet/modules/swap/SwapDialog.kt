package com.payfunds.wallet.modules.swap

import android.text.TextPaint
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.payfunds.wallet.R
import com.payfunds.wallet.ui.compose.ColoredTextStyle
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.animations.shake
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.HsSwitch
import com.payfunds.wallet.ui.compose.components.body_grey
import com.payfunds.wallet.ui.compose.components.body_leah
import com.payfunds.wallet.ui.compose.components.headline1_leah
import java.math.BigDecimal

@Composable
fun SwapSettingsDialog(
    onCloseClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
    var switchChecked by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onCancelClick
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(color = ComposeAppTheme.colors.lawrence)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                headline1_leah(text = stringResource(R.string.SwapSettings_Title_Dialog))
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = onCancelClick
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_close),
                        contentDescription = "Close",
                        tint = ComposeAppTheme.colors.grey
                    )
                }
            }
            body_grey(text = stringResource(R.string.SwapSettings_Description_Dialog))
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                body_leah(text = stringResource(R.string.Swap_Automatic_Slippage_Tolerance_Dialog))
                Spacer(Modifier.weight(1f))
                IconButton(
                    onClick = onCancelClick
                ) {
                    HsSwitch(
                        checked = switchChecked,
                        onCheckedChange = { switchChecked = it }
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    body_grey(text = stringResource(R.string.Swap_Slippage_Dialog))
                    Spacer(Modifier.width(4.dp))
                    Icon(
                        modifier = Modifier.size(18.dp),
                        painter = painterResource(R.drawable.ic_info_20),
                        contentDescription = "Close",
                        tint = ComposeAppTheme.colors.grey
                    )
                }
                Spacer(Modifier.weight(1f))
                body_grey(text = "0.5%")
            }

            if (!switchChecked) {
                Spacer(Modifier.height(24.dp))
                AdjustablePercentageRow(
                    initialPercentage = BigDecimal.ZERO,
                    onPercentageChange = {}
                )

            }
            Spacer(Modifier.height(32.dp))

            ButtonPrimaryRed(
                onClick = onCloseClick,
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.SwapSettings_Close)
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewSwapSettingsDialog() {
    ComposeAppTheme {
        SwapSettingsDialog(
            {}, {},
        )
    }
}

@Composable
fun AppendTextFieldComponent(
    trailing: String = "%",
    value: BigDecimal?,
    onValueChange: (BigDecimal?) -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val textFieldValueSaver = Saver<TextFieldValue, Pair<String, TextRange>>(
        save = { textFieldValue -> textFieldValue.text to textFieldValue.selection },
        restore = { (text, selection) -> TextFieldValue(text = text, selection = selection) }
    )

    var textState by rememberSaveable(stateSaver = textFieldValueSaver) {
        mutableStateOf(
            TextFieldValue(
                value?.toPlainString() ?: "0.00"
            )
        )
    }

    var playShakeAnimation by remember { mutableStateOf(false) }

    val density = LocalDensity.current
    val textSizePx = with(density) { ComposeAppTheme.typography.body.fontSize.toPx() }

    LaunchedEffect(value) {
        textState = TextFieldValue(value?.toPlainString() ?: "0.00")
    }

    Row(
        modifier = Modifier
            .border(
                width = 1.dp,
                color = ComposeAppTheme.colors.steel20,
                shape = RoundedCornerShape(6.dp)
            )
            .padding(6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val textMeasureResult = remember(textState.text, textSizePx) {
            val textPaint = TextPaint().apply {
                textSize = textSizePx
            }
            textPaint.measureText(textState.text.ifEmpty { "0.00" })
        }

        BasicTextField(
            modifier = Modifier
                .widthIn(min = 70.dp, max = with(density) { textMeasureResult.toDp() })
                .focusRequester(focusRequester)
                .shake(
                    enabled = playShakeAnimation,
                    onAnimationFinish = { playShakeAnimation = false }
                ),
            value = textState,
            singleLine = true,
            onValueChange = { textFieldValue ->
                val newValue = textFieldValue.text.toBigDecimalOrNull()
                newValue?.let {
                    val constrainedValue = it.coerceIn(BigDecimal("0.00"), BigDecimal("100.00"))
                    textState =
                        TextFieldValue(constrainedValue.toPlainString(), textFieldValue.selection)
                    onValueChange(constrainedValue)
                }
            },
            textStyle = ColoredTextStyle(
                color = ComposeAppTheme.colors.leah,
                textStyle = ComposeAppTheme.typography.body
            ),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            cursorBrush = SolidColor(ComposeAppTheme.colors.jacob),
            decorationBox = { innerTextField ->
                if (textState.text.isEmpty()) {
                    body_leah(text = "0.00")
                }
                innerTextField()
            }
        )
        body_leah(
            text = trailing,
        )
    }
}

@Composable
fun AdjustablePercentageRow(
    initialPercentage: BigDecimal = BigDecimal.ZERO,
    onPercentageChange: (BigDecimal) -> Unit
) {
    var percentageValue by rememberSaveable { mutableStateOf(initialPercentage) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = ComposeAppTheme.colors.steel20,
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 16.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(
            onClick = {
                percentageValue =
                    (percentageValue - BigDecimal("0.5")).coerceAtLeast(BigDecimal.ZERO)
                onPercentageChange(percentageValue)
            }
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.ic_swap_circle_min),
                contentDescription = "Decrease",
                tint = ComposeAppTheme.colors.leah
            )
        }

        AppendTextFieldComponent(
            trailing = "%",
            value = percentageValue,
            onValueChange = { newValue ->
                newValue?.let {
                    percentageValue =
                        it.coerceAtLeast(BigDecimal.ZERO).coerceAtMost(BigDecimal("100"))
                    onPercentageChange(percentageValue)
                }
            }
        )

        IconButton(
            onClick = {
                percentageValue =
                    (percentageValue + BigDecimal("0.5")).coerceAtMost(BigDecimal("100"))
                onPercentageChange(percentageValue)
            }
        ) {
            Icon(
                modifier = Modifier.size(24.dp),
                painter = painterResource(R.drawable.ic_swap_circle_max),
                contentDescription = "Increase",
                tint = ComposeAppTheme.colors.leah
            )
        }
    }
}

