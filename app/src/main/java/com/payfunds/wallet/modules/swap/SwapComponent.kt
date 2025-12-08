package com.payfunds.wallet.modules.swap

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.payfunds.wallet.R
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.subhead1_leah

@Composable
fun TokenSelectorRow(
    tokenIcon: Int? = null,
    tokenName: String,
    onClick: (() -> Unit)? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(shape = RoundedCornerShape(12.dp))
            .clickable { onClick?.invoke() }
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        tokenIcon?.let {
            Image(
                modifier = Modifier
                    .size(18.dp)
                    .padding(end = 4.dp),
                painter = painterResource(tokenIcon),
                contentDescription = null,
            )
        }

        subhead1_leah(
            text = tokenName,
            maxLines = 1
        )
        Icon(
            modifier = Modifier
                .size(18.dp)
                .padding(start = 6.dp),
            painter = painterResource(R.drawable.ic_arrow_big_down_20),
            contentDescription = null,
            tint = ComposeAppTheme.colors.grey
        )
    }
}
