package com.payfunds.wallet.core.ethereum

import io.horizontalsystems.marketkit.models.Token
import com.payfunds.wallet.core.Clearable
import com.payfunds.wallet.core.managers.CurrencyManager
import com.payfunds.wallet.core.managers.MarketKitWrapper
import com.payfunds.wallet.entities.CoinValue
import com.payfunds.wallet.entities.CurrencyValue
import com.payfunds.wallet.modules.send.SendModule
import java.math.BigDecimal
import java.math.BigInteger

class EvmCoinService(
    val token: Token,
    private val currencyManager: CurrencyManager,
    private val marketKit: MarketKitWrapper
) : Clearable {

    val rate: CurrencyValue?
        get() {
            val baseCurrency = currencyManager.baseCurrency
            return marketKit.coinPrice(token.coin.uid, baseCurrency.code)?.let {
                CurrencyValue(baseCurrency, it.value)
            }
        }

    fun amountData(value: BigInteger, approximate: Boolean = false): SendModule.AmountData {
        val decimalValue = BigDecimal(value, token.decimals)
        val coinValue = CoinValue(token, decimalValue)

        val primaryAmountInfo = SendModule.AmountInfo.CoinValueInfo(coinValue, approximate)
        val secondaryAmountInfo = rate?.let {
            SendModule.AmountInfo.CurrencyValueInfo(
                CurrencyValue(
                    it.currency,
                    it.value * decimalValue
                ), approximate
            )
        }

        return SendModule.AmountData(primaryAmountInfo, secondaryAmountInfo)
    }

    fun amountData(value: BigDecimal): SendModule.AmountData {
        return amountData(value.movePointRight(token.decimals).toBigInteger())
    }

    fun coinValue(value: BigInteger): CoinValue {
        return CoinValue(token, convertToMonetaryValue(value))
    }

    fun convertToMonetaryValue(value: BigInteger): BigDecimal {
        return value.toBigDecimal().movePointLeft(token.decimals).stripTrailingZeros()
    }

    fun convertToFractionalMonetaryValue(value: BigDecimal): BigInteger {
        return value.movePointRight(token.decimals).toBigInteger()
    }

    override fun clear() = Unit
}
