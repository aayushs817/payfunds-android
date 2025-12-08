package com.payfunds.wallet.modules.multiswap

import android.os.Parcelable
import io.horizontalsystems.marketkit.models.Token
import com.payfunds.wallet.entities.CurrencyValue
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal

@Parcelize
data class CoinBalanceItem(
    val token: Token,
    val balance: BigDecimal?,
    val fiatBalanceValue: CurrencyValue?,
) : Parcelable
