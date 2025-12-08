package com.payfunds.wallet.network.response_model.get_tokens

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class CoinDataModel(
    val _id: String,
    val address: String,
    val createdAt: String,
    val decimals: Int,
    val img: String,
    val isUnRegistered: Boolean,
    val last_updated_at: Int,
    val market_cap: Double,
    val name: String,
    val price: BigDecimal,
    val price_change_24h: Double,
    val relatedTokens: List<RelatedTokenModel>,
    val ticker: String,
    val tickerId: String,
    val updatedAt: String,
    val volume_24h: Double
) : Parcelable