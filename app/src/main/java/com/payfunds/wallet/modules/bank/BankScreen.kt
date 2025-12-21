package com.payfunds.wallet.modules.bank

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payfunds.wallet.R
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.*

@Composable
fun BankScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeAppTheme.colors.tyler)
    ) {
        AppBar(
            title = stringResource(R.string.Bank_MyCards),
        )

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Create Card Banner
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ComposeAppTheme.colors.lawrence)
                    .clickable { /* Handle Create Card Click */ },
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .clip(CircleShape)
                            .background(ComposeAppTheme.colors.jacob),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_plus_20),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.Bank_CreateCard),
                        style = ComposeAppTheme.typography.headline1,
                        color = ComposeAppTheme.colors.leah
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.Bank_CreateCardSubtitle),
                        style = ComposeAppTheme.typography.subhead2,
                        color = ComposeAppTheme.colors.grey
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Grid
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionItem(
                    text = stringResource(R.string.Bank_CardControl),
                    iconRes = R.drawable.ic_bank_24,
                    modifier = Modifier.weight(1f)
                )
                ActionItem(
                    text = stringResource(R.string.Transactions_Title),
                    iconRes = R.drawable.ic_list_24,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionItem(
                    text = stringResource(R.string.Bank_Deposit),
                    iconRes = R.drawable.ic_arrow_down_circle_24,
                    modifier = Modifier.weight(1f)
                )
                ActionItem(
                    text = stringResource(R.string.Bank_Withdraw),
                    iconRes = R.drawable.ic_arrow_up_circle_24,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Transactions Header
            Text(
                text = stringResource(R.string.Bank_RecentTransactions),
                style = ComposeAppTheme.typography.body,
                color = ComposeAppTheme.colors.leah,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Recent Transactions List
            val transactions = listOf(
                TransactionViewItem("Grocery Store", "Today", "-$125.50", false, R.drawable.ic_arrow_up_circle_24, ComposeAppTheme.colors.jacob),
                TransactionViewItem("Salary Deposit", "Yesterday", "+$3,500.00", true, R.drawable.ic_arrow_down_circle_24, ComposeAppTheme.colors.remus),
                TransactionViewItem("Coffee Shop", "2 days ago", "-$4.50", false, R.drawable.ic_arrow_up_circle_24, ComposeAppTheme.colors.jacob)
            )

            CellUniversalLawrenceSection(
                items = transactions,
                showFrame = true
            ) { item ->
                TransactionItem(
                    title = item.title,
                    date = item.date,
                    amount = item.amount,
                    isIncoming = item.isIncoming,
                    iconRes = item.iconRes,
                    color = item.color
                )
            }
        }
    }
}

data class TransactionViewItem(
    val title: String,
    val date: String,
    val amount: String,
    val isIncoming: Boolean,
    val iconRes: Int,
    val color: Color
)

@Composable
fun ActionItem(
    text: String,
    iconRes: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(ComposeAppTheme.colors.lawrence)
            .clickable { },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = ComposeAppTheme.colors.jacob,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = ComposeAppTheme.typography.subhead2,
                color = ComposeAppTheme.colors.grey
            )
        }
    }
}

@Composable
fun TransactionItem(
    title: String,
    date: String,
    amount: String,
    isIncoming: Boolean,
    iconRes: Int,
    color: Color
) {
    RowUniversal(
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)), // Semi-transparent background
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = ComposeAppTheme.typography.body,
                color = ComposeAppTheme.colors.leah
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = date,
                style = ComposeAppTheme.typography.subhead2,
                color = ComposeAppTheme.colors.grey
            )
        }
        Text(
            text = amount,
            style = ComposeAppTheme.typography.body,
            color = if (isIncoming) ComposeAppTheme.colors.remus else ComposeAppTheme.colors.jacob
        )
    }
}
