package com.payfunds.wallet.modules.bank

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.payfunds.wallet.core.BaseComposeFragment

class CardControlFragment : BaseComposeFragment() {
    @Composable
    override fun GetContent(navController: NavController) {
        CardControlScreen(navController)
    }
}
