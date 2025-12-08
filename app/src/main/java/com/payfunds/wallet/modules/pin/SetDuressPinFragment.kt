package com.payfunds.wallet.modules.pin

import android.os.Parcelable
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.getInput
import com.payfunds.wallet.modules.pin.ui.PinSet
import io.payfunds.core.helpers.HudHelper
import kotlinx.parcelize.Parcelize

class SetDuressPinFragment : BaseComposeFragment(screenshotEnabled = false) {

    @Composable
    override fun GetContent(navController: NavController) {
        val viewModel = viewModel<SetDuressPinViewModel>(
            factory = SetDuressPinViewModel.Factory(navController.getInput())
        )
        val view = LocalView.current
        PinSet(
            title = stringResource(id = R.string.SetDuressPin_Title),
            description = stringResource(id = R.string.SetDuressPin_Description),
            dismissWithSuccess = {
                viewModel.onDuressPinSet()
                HudHelper.showSuccessMessage(view, R.string.Hud_Text_Created)
                navController.popBackStack(R.id.setDuressPinIntroFragment, true)
            },
            onBackPress = { navController.popBackStack() },
            forDuress = true
        )
    }

    @Parcelize
    data class Input(val accountIds: List<String>) : Parcelable
}
