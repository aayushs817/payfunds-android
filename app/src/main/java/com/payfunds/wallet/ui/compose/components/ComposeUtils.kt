package com.payfunds.wallet.ui.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import io.horizontalsystems.marketkit.models.Coin
import io.horizontalsystems.marketkit.models.Token
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.alternativeImageUrl
import com.payfunds.wallet.core.iconPlaceholder
import com.payfunds.wallet.core.imagePlaceholder
import com.payfunds.wallet.core.imageUrl
import com.payfunds.wallet.modules.market.Value
import com.payfunds.wallet.network.response_model.get_tokens.SToken
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import java.math.BigDecimal

@Composable
fun diffColor(value: BigDecimal?): Color {
    val diff = value ?: BigDecimal.ZERO
    return when {
        diff.signum() == 0 -> ComposeAppTheme.colors.grey
        diff.signum() >= 0 -> ComposeAppTheme.colors.remus
        else -> ComposeAppTheme.colors.lucian
    }
}

@Composable
fun formatValueAsDiff(value: Value): String =
    App.numberFormatter.formatValueAsDiff(value)

@Composable
fun diffText(diff: BigDecimal?): String {
    if (diff == null) return ""
    val sign = when {
        diff == BigDecimal.ZERO -> ""
        diff >= BigDecimal.ZERO -> "+"
        else -> "-"
    }
    return App.numberFormatter.format(diff.abs(), 0, 2, sign, "%")
}

@Composable
fun CoinImage(
    coin: Coin?,
    modifier: Modifier,
    colorFilter: ColorFilter? = null
) = HsImage(
    url = coin?.imageUrl,
    alternativeUrl = coin?.alternativeImageUrl,
    placeholder = coin?.imagePlaceholder,
    modifier = modifier.clip(CircleShape),
    colorFilter = colorFilter
)

@Composable
fun CoinImage(
    token: Token?,
    modifier: Modifier,
    colorFilter: ColorFilter? = null
) = HsImageCircle(
    modifier,
    url = if (token?.coin?.code.equals("EON", true)) null else token?.coin?.imageUrl,
    icon = if (token?.coin?.code.equals("EON", true)) R.drawable.eonix else null,
    token?.coin?.alternativeImageUrl,
    token?.iconPlaceholder,
    colorFilter
)

@Composable
fun SCoinImage(
    token: SToken?,
    modifier: Modifier,
    colorFilter: ColorFilter? = null
) = HsImageCircle(
    modifier,
    token?.img,
    icon = null,
    token?.img,
    R.drawable.coin_placeholder,
    colorFilter
)




@Composable
fun HsImageCircle(
    modifier: Modifier,
    url: String?,
    icon : Int?= null,
    alternativeUrl: String? = null,
    placeholder: Int? = null,
    colorFilter: ColorFilter? = null
) {
    HsImage(
        url = url,
        icon = icon,
        alternativeUrl = alternativeUrl,
        placeholder = placeholder,
        modifier = modifier.clip(CircleShape),
        colorFilter = colorFilter
    )
}

@Composable
fun HsImage(
    url: String?,
    icon: Int?=null,
    alternativeUrl: String? = null,
    placeholder: Int? = null,
    modifier: Modifier,
    colorFilter: ColorFilter? = null
) {
    val fallback = placeholder ?: R.drawable.coin_placeholder
    when {
        url != null -> Image(
            painter = rememberAsyncImagePainter(
                model = url,
                error = alternativeUrl?.let {
                    rememberAsyncImagePainter(
                        model = alternativeUrl,
                        error = painterResource(fallback)
                    )
                } ?: painterResource(fallback)
            ),
            contentDescription = null,
            modifier = modifier,
            colorFilter = colorFilter,
            contentScale = ContentScale.FillBounds
        )

        icon != null -> Image(
            painter = painterResource(icon),
            contentDescription = null,
            modifier = modifier,
            colorFilter = colorFilter,
            contentScale = ContentScale.FillBounds
        )

        else -> Image(
            painter = painterResource(fallback),
            contentDescription = null,
            modifier = modifier,
            colorFilter = colorFilter
        )
    }
}

@Composable
fun NftIcon(
    modifier: Modifier = Modifier,
    iconUrl: String?,
    placeholder: Int? = null,
    colorFilter: ColorFilter? = null
) {
    val fallback = placeholder ?: R.drawable.ic_platform_placeholder_24
    when {
        iconUrl != null -> Image(
            painter = rememberAsyncImagePainter(
                model = iconUrl,
                error = painterResource(fallback)
            ),
            contentDescription = null,
            modifier = modifier
                .clip(RoundedCornerShape(8.dp))
                .size(32.dp),
            colorFilter = colorFilter,
            contentScale = ContentScale.Crop
        )

        else -> Image(
            painter = painterResource(fallback),
            contentDescription = null,
            modifier = modifier.size(32.dp),
            colorFilter = colorFilter
        )
    }
}