package com.payfunds.wallet.modules.alert

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.getWalletAddress
import com.payfunds.wallet.modules.evmfee.ButtonsGroupWithShade
import com.payfunds.wallet.network.request_model.coinapi.CoinAPIRequestModel
import com.payfunds.wallet.network.response_model.alert.list.groupAlertsByToken
import com.payfunds.wallet.network.response_model.alert.list.grouped.TokenAlert
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.HSSwipeRefresh
import com.payfunds.wallet.ui.compose.components.HSCircularProgressIndicator
import com.payfunds.wallet.ui.compose.components.VSpacer

@Composable
fun AlertAllScreen(
    onBackClick: () -> Unit,
) {
    var showAlertSelected by remember { mutableStateOf(false) }
    val viewModelCoin: CoinAPIViewModel = viewModel(factory = CoinAPIModule.Factory())
    val viewModel: AlertViewModel = viewModel(factory = AlertModule.Factory())

    val groupedList = remember { mutableStateListOf<TokenAlert>() }
    val selectedAlertIds = remember { mutableStateListOf<String>() }
    var isEditMode by remember { mutableStateOf(false) }
    var isRefreshing by remember { mutableStateOf(false) }
    var selectAll by remember { mutableStateOf(false) }
    var walletAddress by remember { mutableStateOf("") }

    val isEmpty by viewModel.isEmpty
    val isLoading by viewModel.isApiLoading
    val alertListResponse by viewModel.alertListResponse
    val alertCancelResponse by viewModel.alertCancelResponse

    LaunchedEffect(Unit) {
        val account = App.accountManager.activeAccount
        account?.let {
            val address = getWalletAddress(it)
            walletAddress = address ?: ""
        }
        viewModel.alertList(walletAddress = walletAddress)
    }


    LaunchedEffect(alertListResponse) {
        alertListResponse?.data?.alerts?.let { alerts ->
            groupedList.clear()
            groupedList.addAll(groupAlertsByToken(alerts))
            val requestModel = CoinAPIRequestModel(
                type = "subscribe",
                heartbeat = false,
                subscribe_data_type = listOf("trade"),
                subscribe_filter_exchange_id = listOf("BINANCE"),
                subscribe_update_limit_ms_quote = 1000,
                subscribe_filter_asset_id = groupedList.map { "${it.symbol}/USDT" }
            )
            viewModelCoin.sendCoinAPIRequests(requestModel)
        }
    }

    LaunchedEffect(alertCancelResponse) {
        alertCancelResponse?.let {
            groupedList.clear()
            selectAll = false
            selectedAlertIds.clear()
            viewModel.alertListResponse.value = null
            viewModel.alertCancelResponse.value = null
            viewModel.alertList(walletAddress = walletAddress)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            viewModelCoin.disconnectWebSocket()
        }
    }


    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = ComposeAppTheme.colors.tyler,
        topBar = {
            Box {
                if (isEditMode) {
                    AppBarAlert(
                        onBackClick = { isEditMode = false },
                        title = stringResource(R.string.alert_edit),
                        onDoneClick = { isEditMode = false }
                    )

                } else {
                    AppBarAlert(
                        onBackClick = onBackClick,
                        title = stringResource(R.string.alert_all),
                        onEditClick = if (!isEmpty) {
                            { isEditMode = true }
                        } else null,
                        onInfoClick = { showAlertSelected = true }
                    )
                }
            }
        },
        bottomBar = {
            if (isEditMode) {
                ButtonsGroupWithShade {
                    ItemSelectAllComponent(
                        isChecked = selectAll,
                        isDeleteEnabled = selectedAlertIds.isNotEmpty(),
                        onCheckedChange = { isChecked ->
                            selectAll = isChecked
                            if (selectAll) {
                                groupedList.forEach { groupList ->
                                    groupList.alerts.forEach { alertList ->
                                        selectedAlertIds.add(alertList._id)
                                    }
                                }
                            } else {
                                selectedAlertIds.clear()
                            }
                        },
                        onDeleteClick = {
                            viewModel.alertCancel(
                                alerts = selectedAlertIds.toList(),
                                walletAddress = walletAddress
                            )
                            selectAll = false
                            isEditMode = false
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        HSSwipeRefresh(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxWidth(),
            refreshing = isRefreshing,
            onRefresh = {
                isRefreshing = true
                viewModel.alertList(walletAddress = walletAddress)
                isRefreshing = false
            },
            content = {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(ComposeAppTheme.colors.tyler),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    if (isLoading) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            HSCircularProgressIndicator()
                        }
                    } else if (isEmpty) {
                        isEditMode = false
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            AlertEmptyComponent()
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            items(
                                count = groupedList.size,
                                key = { index -> groupedList[index]._id }
                            ) { index ->
                                ItemAllComponent(
                                    viewModelCoinAPI = viewModelCoin,
                                    alert = groupedList[index],
                                    isEditMode = isEditMode,
                                    selectedAlertIds = selectedAlertIds,
                                    onSelectionChange = { isSelected, ids ->
                                        if (isSelected) {
                                            ids.forEach { id ->
                                                selectedAlertIds.add(id)
                                            }
                                        } else {
                                            ids.forEach { id ->
                                                selectedAlertIds.remove(id)
                                            }
                                        }
                                    },
                                    onDeleteClick = { id ->
                                        viewModel.alertCancel(
                                            alerts = listOf(id),
                                            walletAddress = walletAddress
                                        )
                                    },
                                )
                            }
                        }
                    }
                    VSpacer(32.dp)
                }
            }
        )
        if (showAlertSelected) {
            AlertInfoDialog(
                onDismissRequest = {
                    showAlertSelected = false
                },
            )
        }
    }
}

@Preview
@Composable
fun AlertAllScreenPreview() {
    ComposeAppTheme {
        AlertAllScreen(
            onBackClick = {},
        )
    }
}