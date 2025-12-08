package com.payfunds.wallet.modules.pin

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.setNavigationResultX
import com.payfunds.wallet.modules.pin.ui.PinConfirm
import kotlinx.parcelize.Parcelize

class ConfirmPinFragment : BaseComposeFragment(screenshotEnabled = false) {

    @Composable
    override fun GetContent(navController: NavController) {
        PinConfirm(
            onSuccess = {
                navController.setNavigationResultX(Result(true))
                navController.popBackStack()
            },
            onCancel = {
                navController.popBackStack()
            }
        )
    }

    @Parcelize
    data class Result(val success: Boolean) : Parcelable
}
