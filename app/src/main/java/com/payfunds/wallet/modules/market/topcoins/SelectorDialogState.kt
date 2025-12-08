package com.payfunds.wallet.modules.market.topcoins

import com.payfunds.wallet.modules.market.SortingField
import com.payfunds.wallet.ui.compose.Select

sealed class SelectorDialogState {
    object Closed : SelectorDialogState()
    class Opened(val select: Select<SortingField>) : SelectorDialogState()
}
