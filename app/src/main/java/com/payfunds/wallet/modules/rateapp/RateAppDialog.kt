package com.payfunds.wallet.modules.rateapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.payfunds.wallet.R
import com.payfunds.wallet.core.getGreeting
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryTransparent
import com.payfunds.wallet.ui.compose.components.body_leah
import com.payfunds.wallet.ui.compose.components.title3_leah

@Composable
fun RateApp(
    onRateClick: () -> Unit,
    onCancelClick: () -> Unit,
) {
   val context = LocalContext.current
    Dialog(
        onDismissRequest = onCancelClick
    ) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(16.dp))
                .background(color = ComposeAppTheme.colors.lawrence)
                .padding(horizontal = 24.dp, vertical = 20.dp)
        ) {
            title3_leah(text = context.getGreeting())
            Spacer(Modifier.height(12.dp))
            body_leah(text = stringResource(R.string.RateApp_Description_Rate_This_App))
            Spacer(Modifier.height(32.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                ButtonPrimaryTransparent(
                    onClick = onCancelClick,
                    title = stringResource(R.string.RateApp_Button_NotNow)
                )

                Spacer(Modifier.width(8.dp))

                ButtonPrimaryRed(
                    onClick = onRateClick,
                    title = stringResource(R.string.RateApp_Button_RateIt)
                )
            }
        }
    }
}

@Preview
@Composable
private fun Preview_RateApp() {
    ComposeAppTheme {
        RateApp({}, {})
    }
}
