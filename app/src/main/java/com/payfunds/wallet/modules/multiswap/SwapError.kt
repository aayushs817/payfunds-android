package com.payfunds.wallet.modules.multiswap

sealed class SwapError : Throwable() {
    object InsufficientBalanceFrom : SwapError()
}
