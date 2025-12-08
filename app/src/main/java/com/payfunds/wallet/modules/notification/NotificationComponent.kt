package com.payfunds.wallet.modules.notification

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payfunds.wallet.R
import com.payfunds.wallet.core.convertToDateWithTime
import com.payfunds.wallet.network.response_model.get_all_notification.Data
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.HSpacer
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.compose.components.caption_grey
import com.payfunds.wallet.ui.compose.components.subhead2_leah


@Composable
fun NotificationItem(
    clickItem: () -> Unit = {},
    notification: Data
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = if (notification.isRead)
                    ComposeAppTheme.colors.tyler
                else
                    ComposeAppTheme.colors.lawrence,
            )
            .clickable(onClick = { clickItem() })
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    end = 12.dp,
                    top = 12.dp,
                    bottom = 12.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_notification),
                contentDescription = "Notification",
                tint = ComposeAppTheme.colors.redG,
            )
            HSpacer(8.dp)
            Column {
                if (notification.isRead) {
                    subhead2_leah(
                        modifier = Modifier.fillMaxWidth(),
                        text = notification.description,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = notification.description,
                        modifier = Modifier.fillMaxWidth(),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = ComposeAppTheme.colors.leah,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                VSpacer(6.dp)
                caption_grey(
                    modifier = Modifier.fillMaxWidth(),
                    text = notification.createdAt?.convertToDateWithTime() ?: "",
                    maxLines = 1,
                    textAlign = TextAlign.End
                )
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = ComposeAppTheme.colors.steel10
        )
    }

}
/*
@Composable
fun NotificationItem2(
    clickItem: () -> Unit = {},
    notification: Data
) {
    Column {
        VSpacer(6.dp)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
                .background(
                    color = if (notification.isRead)
                        ComposeAppTheme.colors.tyler
                    else
                        ComposeAppTheme.colors.lawrence,
                    shape = RoundedCornerShape(6.dp)
                )
                .clickable(onClick = { clickItem() })
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_notification),
                    contentDescription = "Notification",
                    tint = ComposeAppTheme.colors.redG,
                )
                HSpacer(8.dp)
                Column {
                    if (notification.isRead) {
                        subhead2_leah(
                            modifier = Modifier.fillMaxWidth(),
                            text = notification.description,
                            maxLines = 1
                        )
                    } else {
                        Text(
                            text = notification.description,
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 1,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium,
                            color = ComposeAppTheme.colors.leah,
                        )
                    }
                    VSpacer(6.dp)
                    caption_grey(
                        modifier = Modifier.fillMaxWidth(),
                        text = notification.createdAt?.convertToDateWithTime() ?: "",
                        maxLines = 1,
                        textAlign = TextAlign.End
                    )
                }
            }
        }
        HorizontalDivider(
            thickness = 1.dp,
            color = ComposeAppTheme.colors.steel20
        )
    }

}

@Preview(showBackground = true)
@Composable
fun TransactionsItemPreview() {
    ComposeAppTheme {
        NotificationItem(
            clickItem = {},
            notification = Data(
                title = "Alert: Bitcoin price reached $60,656",
                _account = "String",
                _id = "String",
                description = "String",
                isRead = false,
                metadata = Metadata(
                    token = Token(
                        name = "",
                        symbol = ""
                    )
                ),
                readAt = "String",
            )
        )
    }
}
*/