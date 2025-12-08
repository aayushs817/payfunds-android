package com.payfunds.wallet.modules.alert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.payfunds.wallet.R
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.AlertHeader
import com.payfunds.wallet.ui.compose.components.AlertItem
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.compose.components.headline1_leah
import com.payfunds.wallet.ui.compose.components.subhead1_grey

data class AlertDropItem(
    val symbol: String,
    val list: String
)

@Composable
fun AlertTypeDialog(
    selectedAlert: AlertDropItem, onAlertSelected: (AlertDropItem) -> Unit,
    onDismissRequest: () -> Unit
) {
    val alertOptions = listOf(
        AlertDropItem(symbol = "$", list = AlertTypes.PRICE_REACHES.title),
        AlertDropItem(symbol = "$", list = AlertTypes.PRICE_RISES_ABOVE.title),
        AlertDropItem(symbol = "$", list = AlertTypes.PRICE_DROP_TO.title),
        AlertDropItem(symbol = "%", list = AlertTypes.CHANGE_IS_OVER.title),
        AlertDropItem(symbol = "%", list = AlertTypes.CHANGE_IS_UNDER.title),
    )
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ComposeAppTheme.colors.lawrence)
            ) {
                AlertHeader(R.string.alert_type)
                alertOptions.forEach { alertType ->
                    AlertItem(
                        onClick = { onAlertSelected(alertType) }
                    ) {
                        Text(
                            text = alertType.list,
                            color = if (alertType == selectedAlert) ComposeAppTheme.colors.jacob else ComposeAppTheme.colors.leah,
                            style = ComposeAppTheme.typography.body,
                        )
                    }
                }
                ButtonPrimaryRed(
                    onClick = { onDismissRequest() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 16.dp, end = 16.dp, bottom = 16.dp, top = 8.dp),
                    title = stringResource(R.string.cancel)
                )
            }
    }

}



@Composable
fun AlertInfoDialog(
    onDismissRequest: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(dismissOnClickOutside = false)
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(color = ComposeAppTheme.colors.lawrence)
                .padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            headline1_leah(
                text = stringResource(R.string.alert_info_title),
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
            VSpacer(16.dp)
            subhead1_grey(
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                text = stringResource(R.string.alert_info_text)
            )
            VSpacer(32.dp)
            ButtonPrimaryRed(
                onClick = { onDismissRequest() },
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(R.string.alert_ok)
            )
        }
    }
}

@Preview
@Composable
private fun AlertFrequencyDialogPreview() {
    ComposeAppTheme {
        AlertInfoDialog(
            onDismissRequest = {}
        )
    }
}

@Preview
@Composable
private fun AlertTypeDialogPreview() {
    ComposeAppTheme {
        AlertTypeDialog(
            selectedAlert = AlertDropItem(symbol = "$", list = AlertTypes.PRICE_REACHES.title),
            onAlertSelected = {},
            onDismissRequest = {}
        )
    }
}


