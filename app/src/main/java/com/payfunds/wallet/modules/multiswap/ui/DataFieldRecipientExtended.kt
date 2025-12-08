package com.payfunds.wallet.modules.multiswap.ui

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import io.horizontalsystems.marketkit.models.BlockchainType
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.entities.Address
import com.payfunds.wallet.modules.multiswap.QuoteInfoRow
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.ButtonSecondaryCircle
import com.payfunds.wallet.ui.compose.components.HSpacer
import com.payfunds.wallet.ui.compose.components.subhead2_grey
import com.payfunds.wallet.ui.compose.components.subhead2_leah
import com.payfunds.wallet.ui.helpers.TextHelper
import io.payfunds.core.helpers.HudHelper

data class DataFieldRecipientExtended(
    val address: Address,
    val blockchainType: BlockchainType
) : DataField {
    @Composable
    override fun GetContent(navController: NavController, borderTop: Boolean) {
        QuoteInfoRow(
            borderTop = borderTop,
            title = {
                subhead2_grey(text = stringResource(R.string.Swap_Recipient))
            },
            value = {
                subhead2_leah(
                    modifier = Modifier.weight(1f, false),
                    text = address.hex,
                    textAlign = TextAlign.End
                )

                val view = LocalView.current
                HSpacer(16.dp)
                ButtonSecondaryCircle(
                    icon = R.drawable.ic_copy_20,
                    onClick = {
                        TextHelper.copyText(address.hex)
                        HudHelper.showSuccessMessage(view, R.string.Hud_Text_Copied)
                    }
                )
            }
        )

        val contact = App.contactsRepository.getContactsFiltered(
            blockchainType,
            addressQuery = address.hex
        ).firstOrNull()

        contact?.name?.let { name ->
            QuoteInfoRow(
                borderTop = borderTop,
                title = {
                    subhead2_grey(text = stringResource(R.string.TransactionInfo_ContactName))
                },
                value = {
                    subhead2_leah(
                        text = name,
                        textAlign = TextAlign.End
                    )
                }
            )
        }
    }
}

@Preview
@Composable
fun DataFieldRecipientExtendedPreview() {
    val navController = rememberNavController()
    ComposeAppTheme {
        DataFieldRecipientExtended(
            Address("0x1234567890abcdef1234567890abcdef12345678"),
            BlockchainType.Bitcoin
        ).GetContent(navController = navController, borderTop = true)
    }
}
