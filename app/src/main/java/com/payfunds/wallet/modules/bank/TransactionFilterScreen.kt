package com.payfunds.wallet.modules.bank

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.slideFromRight
import com.payfunds.wallet.modules.evmfee.ButtonsGroupWithShade
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.TranslatableString
import com.payfunds.wallet.ui.compose.components.*
import com.payfunds.wallet.ui.compose.components.cell.CellUniversal
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionFilterScreen(
    navController: NavController,
) {
    var filterBy by remember { mutableStateOf("All") }
    var dateRangeExpanded by remember { mutableStateOf(false) }
    var amountRangeExpanded by remember { mutableStateOf(false) }

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    val sdf = remember { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()) }

    if (showStartDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        startDate = sdf.format(Date(it))
                    }
                    showStartDatePicker = false
                }) {
                    Text("OK", color = ComposeAppTheme.colors.jacob)
                }
            },
            dismissButton = {
                TextButton(onClick = { showStartDatePicker = false }) {
                    Text("Cancel", color = ComposeAppTheme.colors.jacob)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    if (showEndDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let {
                        endDate = sdf.format(Date(it))
                    }
                    showEndDatePicker = false
                }) {
                    Text("OK", color = ComposeAppTheme.colors.jacob)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEndDatePicker = false }) {
                    Text("Cancel", color = ComposeAppTheme.colors.jacob)
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(ComposeAppTheme.colors.tyler)
    ) {

        AppBar(
            title = stringResource(R.string.filter),
            navigationIcon = {
                HsBackButton(onClick = { navController.popBackStack()})
            },
            menuItems = listOf(
                MenuItem(
                    title = TranslatableString.PlainString("Reset"),
                    onClick = { /* Handle reset */ },
                    tint = ComposeAppTheme.colors.jacob
                )
            )
        )

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .weight(1f)
        ) {

            Column(
                modifier = Modifier.padding(horizontal = 16.dp),
            ) {
                VSpacer(height = 12.dp)
                headline1_leah(text = "Filter Transactions")
                VSpacer(height = 24.dp)
                subhead2_grey(text = "Filter By")
                VSpacer(height = 12.dp)
            }
            CellUniversalLawrenceSection(
                items = listOf("All Transactions", "Income Only", "Expense Only"),
                showFrame = true
            ) { item ->
                val selected = (item == "All Transactions" && filterBy == "All") ||
                        (item == "Income Only" && filterBy == "Income") ||
                        (item == "Expense Only" && filterBy == "Expense")

                RowUniversal(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    onClick = {
                        filterBy = when (item) {
                            "All Transactions" -> "All"
                            "Income Only" -> "Income"
                            "Expense Only" -> "Expense"
                            else -> "All"
                        }
                    }
                ) {
                    body_leah(text = item)
                    Spacer(modifier = Modifier.weight(1f))
                    if (selected) {
                        Image(
                            painter = painterResource(id = R.drawable.ic_checkmark_20),
                            contentDescription = null,
                            colorFilter = ColorFilter.tint(ComposeAppTheme.colors.lucian)
                        )
                    }
                }
            }

            VSpacer(height = 12.dp)

            // Date Range Expandable
            CellUniversalLawrenceSection(
                items = listOf("Date Range"),
                showFrame = true
            ) {
                Column {
                    RowUniversal(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        onClick = { dateRangeExpanded = !dateRangeExpanded }
                    ) {
                        body_leah(text = "Date Range")
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            painter = painterResource(id = if (dateRangeExpanded) R.drawable.ic_arrow_big_up_20 else R.drawable.ic_arrow_big_down_20),
                            contentDescription = null,
                            tint = ComposeAppTheme.colors.grey
                        )
                    }

                    if (dateRangeExpanded) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    subhead2_grey(text = "Start Date")
                                    VSpacer(height = 8.dp)
                                    Box(modifier = Modifier.clickable { showStartDatePicker = true }) {
                                        FormsInput(
                                            enabled = false,
                                            hint = startDate.ifEmpty { "21 Nov 2025" },
                                            hintColor = if (startDate.isEmpty()) ComposeAppTheme.colors.grey50 else ComposeAppTheme.colors.leah,
                                            onValueChange = {},
                                            qrScannerEnabled = false,
                                            pasteEnabled = false,
                                            state = null
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    subhead2_grey(text = "End Date")
                                    VSpacer(height = 8.dp)
                                    Box(modifier = Modifier.clickable { showEndDatePicker = true }) {
                                        FormsInput(
                                            enabled = false,
                                            hint = endDate.ifEmpty { "21 Dec 2025" },
                                            hintColor = if (endDate.isEmpty()) ComposeAppTheme.colors.grey50 else ComposeAppTheme.colors.leah,
                                            onValueChange = {},
                                            qrScannerEnabled = false,
                                            pasteEnabled = false,
                                            state = null
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            VSpacer(height = 12.dp)

            // Amount Range Expandable
            CellUniversalLawrenceSection(
                items = listOf("Amount Range"),
                showFrame = true
            ) {
                Column {
                    RowUniversal(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        onClick = { amountRangeExpanded = !amountRangeExpanded }
                    ) {
                        body_leah(text = "Amount Range")
                        Spacer(modifier = Modifier.weight(1f))
                        Icon(
                            painter = painterResource(id = if (amountRangeExpanded) R.drawable.ic_arrow_big_up_20 else R.drawable.ic_arrow_big_down_20),
                            contentDescription = null,
                            tint = ComposeAppTheme.colors.grey
                        )
                    }

                    if (amountRangeExpanded) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                Column(modifier = Modifier.weight(1f)) {
                                    subhead2_grey(text = "Minimum Amount")
                                    VSpacer(height = 8.dp)
                                    FormsInput(
                                        hint = "100",
                                        onValueChange = {},
                                        qrScannerEnabled = false,
                                        pasteEnabled = false,
                                        state = null
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    subhead2_grey(text = "Maximum Amount")
                                    VSpacer(height = 8.dp)
                                    FormsInput(
                                        hint = "1000",
                                        onValueChange = {},
                                        qrScannerEnabled = false,
                                        pasteEnabled = false,
                                        state = null
                                    )
                                }
                            }
                        }
                    }
                }
            }

            VSpacer(height = 32.dp)
        }

        ButtonsGroupWithShade {
            ButtonPrimaryRed(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                title = "Apply Filter",
                onClick = { navController.popBackStack() }
            )
        }
    }
}
