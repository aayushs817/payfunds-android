package com.payfunds.wallet.modules.amount

import com.payfunds.wallet.R
import com.payfunds.wallet.core.HSCaution
import com.payfunds.wallet.modules.send.SendErrorInsufficientBalance
import com.payfunds.wallet.modules.send.SendErrorMaximumSendAmount
import com.payfunds.wallet.modules.send.SendErrorMinimumSendAmount
import com.payfunds.wallet.ui.compose.TranslatableString
import java.math.BigDecimal

class AmountValidator {

    fun validate(
        coinAmount: BigDecimal?,
        coinCode: String,
        availableBalance: BigDecimal,
        minimumSendAmount: BigDecimal? = null,
        maximumSendAmount: BigDecimal? = null,
        leaveSomeBalanceForFee: Boolean = false
    ) = when {
        coinAmount == null -> null
        coinAmount == BigDecimal.ZERO -> null
        coinAmount > availableBalance -> {
            SendErrorInsufficientBalance(coinCode)
        }

        minimumSendAmount != null && coinAmount < minimumSendAmount -> {
            SendErrorMinimumSendAmount(minimumSendAmount)
        }

        maximumSendAmount != null && coinAmount > maximumSendAmount -> {
            SendErrorMaximumSendAmount(maximumSendAmount)
        }

        leaveSomeBalanceForFee && coinAmount == availableBalance -> {
            HSCaution(
                TranslatableString.ResString(
                    R.string.EthereumTransaction_Warning_CoinNeededForFee,
                    coinCode
                ),
                HSCaution.Type.Warning
            )
        }

        else -> null
    }

}
