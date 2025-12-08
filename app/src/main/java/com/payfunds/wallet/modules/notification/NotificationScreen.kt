package com.payfunds.wallet.modules.notification

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.slideFromRight
import com.payfunds.wallet.modules.coin.CoinFragment
import com.payfunds.wallet.modules.evmfee.ButtonsGroupWithShade
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.AppBar
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.HsBackButton


class NotificationFragment : BaseComposeFragment() {
    @Composable
    override fun GetContent(navController: NavController) {
        NotificationScreen(navController = navController)
    }
}

@Composable
fun NotificationScreen(
    navController: NavController
) {
    val viewModel = viewModel<NotificationViewModel>(factory = NotificationModule.Factory())

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycleState by lifecycleOwner.lifecycle.currentStateFlow.collectAsState()
    var isMarkAsReadButtonEnabled by remember{ mutableStateOf(false) }

    LaunchedEffect(lifecycleState) {
        when (lifecycleState) {
            Lifecycle.State.DESTROYED -> {}
            Lifecycle.State.INITIALIZED -> {}
            Lifecycle.State.CREATED -> {}
            Lifecycle.State.STARTED -> {}
            Lifecycle.State.RESUMED -> {
                viewModel.getAllNotification(1,100)
            }
        }
    }

    LaunchedEffect(viewModel.notificationMarkAsReadResponseModel.value) {
        isMarkAsReadButtonEnabled = false
        viewModel.getAllNotification(1,100)
    }


    Scaffold(
        modifier = Modifier.statusBarsPadding(),
        containerColor = ComposeAppTheme.colors.tyler,
        topBar = {
            AppBar(
                title = stringResource(R.string.notifications),
                navigationIcon = {
                    HsBackButton(onClick = navController::popBackStack)
                }
            )
        },
        bottomBar = {
            ButtonsGroupWithShade {
                if (isMarkAsReadButtonEnabled) {
                    ButtonPrimaryRed(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        title = stringResource(R.string.notifications_read_all),
                        loadingIndicator = viewModel.isApiLoading.value,
                        enabled = !viewModel.isApiLoading.value,
                        onClick = {
                            viewModel.markAsReadNotification(isMarkAsRead = true)
                        }
                    )
                }
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
        ) {

            viewModel.getAllNotificationResponseModel.value?.data?.let { item ->

                for (i in item){
                    if (!i.isRead){
                        isMarkAsReadButtonEnabled = true
                        break
                    }
                }

                items(item) { notification ->

                    NotificationItem(
                        clickItem = {
                            if (!notification.isRead){
                                viewModel.markAsReadNotification(notificationId = notification._id)
                            }
                            val coin = App.marketKit.allCoins().find { it.code == notification.metadata.token.symbol }
                            coin?.let {
                                val arguments = CoinFragment.Input(it.uid)

                                navController.slideFromRight(R.id.coinFragment, arguments)
                            }
                        },
                        notification = notification
                    )
                }
            }
        }
    }
}


@Preview
@Composable
fun NotificationScreenPreview() {
    ComposeAppTheme {
        NotificationScreen(navController = NavController(LocalContext.current))
    }
}