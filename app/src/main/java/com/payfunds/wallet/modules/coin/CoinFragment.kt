package com.payfunds.wallet.modules.coin

import android.os.Parcelable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.navGraphViewModels
import com.payfunds.wallet.R
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.getInput
import com.payfunds.wallet.core.slideFromBottom
import com.payfunds.wallet.core.slideFromRight
import com.payfunds.wallet.core.stats.StatEvent
import com.payfunds.wallet.core.stats.StatPage
import com.payfunds.wallet.core.stats.stat
import com.payfunds.wallet.core.stats.statTab
import com.payfunds.wallet.modules.alert.AlertCreateScreen
import com.payfunds.wallet.modules.alert.AlertDropItem
import com.payfunds.wallet.modules.alert.AlertFragment
import com.payfunds.wallet.modules.alert.AlertTypeDialog
import com.payfunds.wallet.modules.chart.ChartViewModel
import com.payfunds.wallet.modules.coin.analytics.CoinAnalyticsScreen
import com.payfunds.wallet.modules.coin.coinmarkets.CoinMarketsScreen
import com.payfunds.wallet.modules.coin.overview.CoinOverviewModule

import com.payfunds.wallet.modules.coin.overview.ui.CoinOverviewScreen
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.TranslatableString
import com.payfunds.wallet.ui.compose.components.AppBar
import com.payfunds.wallet.ui.compose.components.HsBackButton
import com.payfunds.wallet.ui.compose.components.ListEmptyView
import com.payfunds.wallet.ui.compose.components.MenuItem
import com.payfunds.wallet.ui.compose.components.TabItem
import com.payfunds.wallet.ui.compose.components.Tabs
import io.payfunds.core.helpers.HudHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize

class CoinFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        val input = navController.getInput<Input>()
        val coinUid = input?.coinUid ?: ""

        CoinScreen(
            coinUid,
            coinViewModel(coinUid),
            navController,
            childFragmentManager
        )
    }

    private fun coinViewModel(coinUid: String): CoinViewModel? = try {
        val viewModel by navGraphViewModels<CoinViewModel>(R.id.coinFragment) {
            CoinModule.Factory(coinUid)
        }
        viewModel
    } catch (e: Exception) {
        null
    }

    @Parcelize
    data class Input(val coinUid: String) : Parcelable
}

@Composable
fun CoinScreen(
    coinUid: String,
    coinViewModel: CoinViewModel?,
    navController: NavController,
    fragmentManager: FragmentManager
) {
    if (coinViewModel != null) {
        CoinTabs(coinViewModel, navController, fragmentManager)
    } else {
        CoinNotFound(coinUid, navController)
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CoinTabs(
    viewModel: CoinViewModel,
    navController: NavController,
    fragmentManager: FragmentManager
) {
    val tabs = viewModel.tabs
    val pagerState = rememberPagerState(initialPage = 0) { tabs.size }
    val coroutineScope = rememberCoroutineScope()
    val view = LocalView.current

    var showAlertSelected by remember { mutableStateOf(false) }

    val vmFactory by lazy { CoinOverviewModule.Factory(viewModel.fullCoin) }
    val chartViewModel = viewModel<ChartViewModel>(factory = vmFactory)
    val uiState = chartViewModel.uiState


    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = viewModel.fullCoin.coin.code,
                navigationIcon = {
                    HsBackButton(onClick = { navController.popBackStack() })
                },
                menuItems = buildList {
                    if(viewModel.fullCoin.coin.code != "USDT") {
                        add(MenuItem(
                            title = TranslatableString.ResString(R.string.alert_create_alert),
                            icon = R.drawable.ic_alert_alarm,
                            tint = ComposeAppTheme.colors.jacob,
                            onClick = {
                                showAlertSelected = true
                            }
                        ))
                    }

                    if (viewModel.isWatchlistEnabled) {
                        if (viewModel.isFavorite) {
                            add(
                                MenuItem(
                                    title = TranslatableString.ResString(R.string.CoinPage_Unfavorite),
                                    icon = R.drawable.ic_filled_star_24,
                                    tint = ComposeAppTheme.colors.jacob,
                                    onClick = {
                                        viewModel.onUnfavoriteClick()

                                        stat(
                                            page = StatPage.CoinPage,
                                            event = StatEvent.RemoveFromWatchlist(viewModel.fullCoin.coin.uid)
                                        )
                                    }
                                )
                            )
                        } else {
                            add(
                                MenuItem(
                                    title = TranslatableString.ResString(R.string.CoinPage_Favorite),
                                    icon = R.drawable.ic_star_24,
                                    onClick = {
                                        viewModel.onFavoriteClick()

                                        stat(
                                            page = StatPage.CoinPage,
                                            event = StatEvent.AddToWatchlist(viewModel.fullCoin.coin.uid)
                                        )
                                    }
                                )
                            )
                        }
                    }
                }
            )
        }
    ) { innerPaddings ->
        Column(
            modifier = Modifier.padding(innerPaddings)
        ) {
            val selectedTab = tabs[pagerState.currentPage]
            val tabItems = tabs.map {
                TabItem(stringResource(id = it.titleResId), it == selectedTab, it)
            }
            Tabs(tabItems, onClick = { tab ->
                coroutineScope.launch {
                    pagerState.scrollToPage(tab.ordinal)

                    stat(page = StatPage.CoinPage, event = StatEvent.SwitchTab(tab.statTab))

                    if (tab == CoinModule.Tab.Details && viewModel.shouldShowSubscriptionInfo()) {
                        viewModel.subscriptionInfoShown()

                        delay(1000)
                        navController.slideFromBottom(R.id.subscriptionInfoFragment)
                    }
                }
            })

            HorizontalPager(
                state = pagerState,
                userScrollEnabled = false
            ) { page ->
                when (tabs[page]) {
                    CoinModule.Tab.Overview -> {
                        CoinOverviewScreen(
                            fullCoin = viewModel.fullCoin,
                            navController = navController
                        )
                    }

                    CoinModule.Tab.Market -> {
                        CoinMarketsScreen(fullCoin = viewModel.fullCoin)
                    }

                    CoinModule.Tab.Details -> {
                        CoinAnalyticsScreen(
                            fullCoin = viewModel.fullCoin,
                            navController = navController,
                            fragmentManager = fragmentManager
                        )
                    }
                }
            }

            viewModel.successMessage?.let {
                HudHelper.showSuccessMessage(view, it)

                viewModel.onSuccessMessageShown()
            }


            if (showAlertSelected) {
                AlertTypeDialog(
                    onAlertSelected = {
                        AlertCreateScreen.tokenName = viewModel.fullCoin.coin.name
                        AlertCreateScreen.tokenSymbol = viewModel.fullCoin.coin.code

                        AlertCreateScreen.currentPrice = uiState.chartHeaderView?.value?.replace("$", "") ?: "__"
                        AlertCreateScreen.typeList = it.list
                        AlertCreateScreen.typeSymbols = it.symbol

                        AlertCreateScreen.frequencyList = "Only once"
                        AlertCreateScreen.frequencySymbols = "$"
                        navController.slideFromRight(
                            R.id.alertFragment,
                            AlertFragment.Input("alertCreateScreen")
                        )
                        showAlertSelected = false
                    },
                    selectedAlert = AlertDropItem(
                        symbol = "%",
                        list = "Price reaches",
                    ),
                    onDismissRequest = {
                        showAlertSelected = false
                    }
                )
            }
        }
    }
}

@Composable
fun CoinNotFound(coinUid: String, navController: NavController) {
    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        backgroundColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = coinUid,
                navigationIcon = {
                    HsBackButton(onClick = { navController.popBackStack() })
                }
            )
        },
        content = {
            ListEmptyView(
                paddingValues = it,
                text = stringResource(R.string.CoinPage_CoinNotFound, coinUid),
                icon = R.drawable.ic_not_available
            )
        }
    )
}
