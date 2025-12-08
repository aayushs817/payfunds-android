package com.payfunds.wallet.modules.send

import com.payfunds.wallet.core.HSCaution
import java.math.BigDecimal


data class SendUiState(
    val availableBalance: BigDecimal,
    val amountCaution: HSCaution?,
    val addressError: Throwable?,
    val canBeSend: Boolean,
    val showAddressInput: Boolean,
)
