package com.payfunds.wallet.modules.intro

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.payfunds.wallet.core.App

object IntroModule {

    class Factory : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return IntroViewModel(App.localStorage) as T
        }
    }

    data class IntroSliderData(
        val title: Int,
        val image: Int,
    )

}
