package com.payfunds.wallet.modules.tonconnect

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.requireInput

class TonConnectNewFragment : BaseComposeFragment() {
    @Composable
    override fun GetContent(navController: NavController) {
        TonConnectNewScreen(navController, navController.requireInput())
    }
}
