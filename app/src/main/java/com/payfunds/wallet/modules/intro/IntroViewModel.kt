package com.payfunds.wallet.modules.intro

import androidx.lifecycle.ViewModel
import com.payfunds.wallet.R
import com.payfunds.wallet.core.ILocalStorage

class IntroViewModel(
    private val localStorage: ILocalStorage
) : ViewModel() {

    val slides = listOf(
        IntroModule.IntroSliderData(
            R.string.ManageAccounts_ImportWalletDescription,
            R.drawable.ic_independence_2,

            ),
        IntroModule.IntroSliderData(
            R.string.ManageAccounts_ImportWalletDescription,
            R.drawable.ic_independence_1
        ),
    )

    fun onStartClicked() {
        localStorage.mainShowedOnce = true
    }

}
