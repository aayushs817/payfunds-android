package com.payfunds.wallet.network.response_model.get_tokens

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SToken(
    val _id: String,
    val address: String,
    val decimals: Int,
    val img: String,
    val name: String,
    val ticker: String
) : Parcelable {

    companion object {
        // Map a RelatedTokenModel to SToken
         fun fromRelatedTokenModel(relatedToken: RelatedTokenModel): SToken {
            return SToken(
                _id = relatedToken._id,
                address = relatedToken.address,
                decimals = relatedToken.decimals,
                img = relatedToken.img,
                name = relatedToken.name,
                ticker = relatedToken.ticker
            )
        }

        // Map a CoinDataModel to a list of SToken
        fun fromCoinDataModel(coinDataModel: CoinDataModel): SToken {
            return SToken(
                _id = coinDataModel._id,
                address = coinDataModel.address,
                decimals = coinDataModel.decimals,
                img = coinDataModel.img,
                name = coinDataModel.name,
                ticker = coinDataModel.ticker
            )
        }
    }
}
