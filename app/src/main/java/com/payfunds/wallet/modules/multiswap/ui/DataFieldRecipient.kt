package com.payfunds.wallet.modules.multiswap.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.entities.Address
import com.payfunds.wallet.modules.multiswap.QuoteInfoRow
import com.payfunds.wallet.ui.compose.components.subhead2_grey
import com.payfunds.wallet.ui.compose.components.subhead2_leah

data class DataFieldRecipient(val address: Address) : DataField {
    @Composable
    override fun GetContent(navController: NavController, borderTop: Boolean) {
        QuoteInfoRow(
            borderTop = borderTop,
            title = {
                subhead2_grey(text = stringResource(R.string.Swap_Recipient))
            },
            value = {
                subhead2_leah(
                    text = address.hex,
                    textAlign = TextAlign.End
                )
            }
        )
    }
}
