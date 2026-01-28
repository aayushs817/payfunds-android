package com.payfunds.wallet.modules.bank

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.providers.Translator
import com.payfunds.wallet.core.slideFromRight
import com.payfunds.wallet.entities.ViewState
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.*
import io.payfunds.core.helpers.DateHelper
import io.payfunds.core.helpers.HudHelper
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TransactionsScreen(
    navController: NavController,
    viewModel: BankTransactionsViewModel = viewModel(factory = BankTransactionsModule.Factory())
) {
    val uiState = viewModel.uiState
    val transactions = viewModel.transactions
    val view = LocalView.current
    val listState = rememberLazyListState()

    LaunchedEffect(uiState) {
        if (uiState is ViewState.Error) {
            HudHelper.showErrorMessage(view, uiState.t.message ?: "An error occurred")
        }
    }

    val shouldLoadMore = remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull() ?: return@derivedStateOf false
            lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 5
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value) {
            viewModel.loadNextPage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(ComposeAppTheme.colors.tyler)
    ) {
        AppBar(
            title = stringResource(R.string.Transactions_Title),
            navigationIcon = {
                HsBackButton(onClick = { navController.popBackStack() })
            }
        )

        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FormsInputSearch(
                        modifier = Modifier.weight(1f),
                        hint = "Search transactions",
                        onValueChange = { /* Handle search */ },
                        startContent = {
                            Icon(
                                modifier = Modifier.size(20.dp),
                                painter = painterResource(id = R.drawable.ic_search),
                                contentDescription = null,
                                tint = ComposeAppTheme.colors.grey
                            )
                        }
                    )
//                    Spacer(modifier = Modifier.width(12.dp))
//                    ButtonSecondaryCircle(
//                        icon = R.drawable.ic_manage_2,
//                        onClick = {
//                            navController.slideFromRight(R.id.transactionFilterScreenFragment)
//                        }
//                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            if (transactions.isEmpty() && uiState is ViewState.Success) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 100.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        subhead2_grey(text = "No transactions found")
                    }
                }
            } else {
                items(transactions) { transaction ->
                    val isIncoming = transaction.type.equals("Credit", ignoreCase = true)
                    val color = if (isIncoming) ComposeAppTheme.colors.remus else ComposeAppTheme.colors.jacob
                    val iconRes = if (isIncoming) R.drawable.`ic_arrow_medium2_down_24` else R.drawable.`ic_arrow_medium2_up_24`
                    
                    val date = try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        sdf.parse(transaction.createdAt)
                    } catch (e: Exception) {
                        null
                    }

                    val formattedDate = date?.let { formatDate(it) } ?: transaction.createdAt

                    TransactionItem(
                        title = transaction.type + " " + (if (isIncoming) "from" else "to") + " " + transaction.transactionId.take(8) + "...",
                        date = formattedDate,
                        amount = (if (isIncoming) "+" else "-") + transaction.currency + " " + String.format("%.2f", transaction.amount),
                        isIncoming = isIncoming,
                        iconRes = iconRes,
                        color = color
                    )
                }

                if (uiState is ViewState.Loading && transactions.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = ComposeAppTheme.colors.jacob,
                                strokeWidth = 2.dp
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun formatDate(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date

    val today = Calendar.getInstance()
    if (calendar[Calendar.YEAR] == today[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR]) {
        return Translator.getString(R.string.Timestamp_Today)
    }

    val yesterday = Calendar.getInstance()
    yesterday.add(Calendar.DAY_OF_MONTH, -1)
    if (calendar[Calendar.YEAR] == yesterday[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == yesterday[Calendar.DAY_OF_YEAR]) {
        return Translator.getString(R.string.Timestamp_Yesterday)
    }

    return DateHelper.shortDate(date, "MMMM d", "MMMM d, yyyy")
}
