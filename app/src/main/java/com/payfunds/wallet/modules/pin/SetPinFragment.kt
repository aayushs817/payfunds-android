package com.payfunds.wallet.modules.pin

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.getInput
import com.payfunds.wallet.core.setNavigationResultX
import com.payfunds.wallet.modules.pin.ui.PinSet
import kotlinx.parcelize.Parcelize

class SetPinFragment : BaseComposeFragment(screenshotEnabled = false) {

    @Composable
    override fun GetContent(navController: NavController) {
        val input = navController.getInput<Input>()

        PinSet(
            title = stringResource(R.string.PinSet_Title),
            description = stringResource(input?.descriptionResId ?: R.string.PinSet_Info),
            dismissWithSuccess = {
                navController.setNavigationResultX(Result(true))
                navController.popBackStack()
            },
            onBackPress = { navController.popBackStack() }
        )
    }

    @Parcelize
    data class Input(val descriptionResId: Int) : Parcelable

    @Parcelize
    data class Result(val success: Boolean) : Parcelable
}
