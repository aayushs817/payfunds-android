package com.payfunds.wallet.modules.bank

import android.os.Parcelable

import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.getInput
import kotlinx.parcelize.Parcelize

class CreateBankUserFragment : BaseComposeFragment() {

    @Parcelize
    data class Input(val initialStep: Int) : Parcelable

    @Composable
    override fun GetContent(navController: NavController) {
        val input = navController.getInput<Input>()
        CreateCardScreen(navController, initialStep = input?.initialStep ?: 1)
    }
}
