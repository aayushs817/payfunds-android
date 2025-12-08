package com.payfunds.wallet.network.response_model.two_factor_auth.qr

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

data class Data(
    val recoveryCode: String,
    val secret: String,
    val secretUri: String
)


@Parcelize
data class RecoveryCode(
    val recoveryCode: String
) : Parcelable
