package com.payfunds.wallet.network.response_model.get_tokens

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RelatedTokenModel(
    val _id: String,
    val address: String,
    val decimals: Int,
    val img: String,
    val name: String,
    val ticker: String
):Parcelable