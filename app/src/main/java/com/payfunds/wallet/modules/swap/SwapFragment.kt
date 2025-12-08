package com.payfunds.wallet.modules.swap

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.getInput
import kotlinx.parcelize.Parcelize

class SwapFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        val input = navController.getInput<Input>()
        val destination = input?.destination ?: ""
        val navControllers = rememberNavController()
        NavHost(navController = navControllers, startDestination = destination, builder = {
            composable("swapTokenScreen") {
                SwapTokenScreen(
                    navControllers,
                    onBackClick = { navController.navigateUp()})
            }
            composable("swapConfirmScreen") { SwapConfirmScreen(navControllers) }
        })
    }

    @Parcelize
    data class Input(val destination: String) : Parcelable
}





