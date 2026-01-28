package com.payfunds.wallet.modules.bank

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.payfunds.wallet.modules.bank.data.CountryModal
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.FormsInputSearch
import com.payfunds.wallet.ui.compose.components.RowUniversal
import com.payfunds.wallet.ui.compose.components.SectionUniversalItem
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.compose.components.body_leah
import com.payfunds.wallet.ui.compose.components.subhead2_grey
import com.payfunds.wallet.ui.extensions.BottomSheetHeader
import com.payfunds.wallet.R

@Composable
fun CountrySelectorBottomSheet(
    countryModalList: List<CountryModal>, // Accepts the list passed from parent
    onCountrySelected: (CountryModal) -> Unit,
    onClose: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    // Filter the list passed in arguments
    val filteredCountries = remember(searchQuery, countryModalList) {
        countryModalList.filter {
            it.name.contains(searchQuery, ignoreCase = true) || it.code.contains(searchQuery)
        }
    }

    BottomSheetHeader(
        iconPainter = painterResource(R.drawable.ic_globe),
        title = "Select Country",
        onCloseClick = onClose
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        ) {
            FormsInputSearch(
                hint = "Search country",
                onValueChange = { searchQuery = it }
            )
            VSpacer(16.dp)
            Column(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.dp, ComposeAppTheme.colors.steel10, RoundedCornerShape(12.dp))
            ) {
                // Limit the list size for performance if needed, or use LazyColumn inside a finite height
                // Since this is inside a column scroll, standard Column is okay for ~200 items
                // but LazyColumn is better practice if the parent supports it.
                // Sticking to your structure:

                filteredCountries.forEachIndexed { index, country ->
                    SectionUniversalItem(borderTop = index != 0) {
                        RowUniversal(
                            modifier = Modifier
                                .clickable { onCountrySelected(country) }
                                .padding(horizontal = 16.dp),
                            verticalPadding = 12.dp
                        ) {
                            Image(
                                painter = painterResource(id = country.flagRes),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            body_leah(
                                modifier = Modifier.weight(1f),
                                text = country.name
                            )
                            subhead2_grey(text = country.code)
                        }
                    }
                }
            }
            VSpacer(32.dp)
        }
    }
}