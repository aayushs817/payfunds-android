package com.payfunds.wallet.modules.chart

import com.payfunds.wallet.core.App
import com.payfunds.wallet.entities.Currency
import java.math.BigDecimal

class ChartCurrencyValueFormatterShortened : ChartModule.ChartNumberFormatter {

    override fun formatValue(currency: Currency, value: BigDecimal): String {
        return App.numberFormatter.formatFiatShort(value, currency.symbol, 2)
    }

}
