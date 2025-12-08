package com.payfunds.wallet.core

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.os.Parcelable
import android.widget.ImageView
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import coil.load
import io.horizontalsystems.ethereumkit.core.signer.Signer
import io.horizontalsystems.ethereumkit.core.toRawHexString
import io.horizontalsystems.hdwalletkit.HDExtendedKey
import io.horizontalsystems.hdwalletkit.HDWallet
import io.horizontalsystems.hdwalletkit.Language
import io.horizontalsystems.hdwalletkit.Mnemonic
import io.horizontalsystems.hodler.LockTimeInterval
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.CoinCategory
import io.horizontalsystems.marketkit.models.CoinInvestment
import io.horizontalsystems.marketkit.models.CoinTreasury
import io.horizontalsystems.marketkit.models.FullCoin
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App.Companion.evmBlockchainManager
import com.payfunds.wallet.entities.Account
import com.payfunds.wallet.entities.AccountType
import com.payfunds.wallet.modules.manageaccount.publickeys.PublicKeysModule.ExtendedPublicKey
import com.payfunds.wallet.modules.manageaccount.showextendedkey.ShowExtendedKeyModule.DisplayKeyType.AccountPublicKey
import com.payfunds.wallet.modules.market.ImageSource
import com.payfunds.wallet.modules.market.topplatforms.Platform
import kotlinx.coroutines.delay
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalTime
import java.util.Locale
import java.util.Optional

val <T> Optional<T>.orNull: T?
    get() = when {
        isPresent -> get()
        else -> null
    }

val Platform.iconUrl: String
    get() = "https://cdn.blocksdecoded.com/blockchain-icons/32px/$uid@3x.png"

val String.coinIconUrl: String
    get() = "https://cdn.blocksdecoded.com/coin-icons/32px/$this@3x.png"

val String.fiatIconUrl: String
    get() = "https://cdn.blocksdecoded.com/fiat-icons/$this@3x.png"

val CoinCategory.imageUrl: String
    get() = "https://cdn.blocksdecoded.com/category-icons/$uid@3x.png"

val CoinInvestment.Fund.logoUrl: String
    get() = "https://cdn.blocksdecoded.com/fund-icons/$uid@3x.png"

val CoinTreasury.logoUrl: String
    get() = "https://cdn.blocksdecoded.com/treasury-icons/$fundUid@3x.png"

fun List<FullCoin>.sortedByFilter(filter: String): List<FullCoin> {
    val baseComparator = compareBy<FullCoin> {
        it.coin.marketCapRank ?: Int.MAX_VALUE
    }.thenBy {
        it.coin.name.lowercase(Locale.ENGLISH)
    }
    val comparator = if (filter.isNotBlank()) {
        val lowercasedFilter = filter.lowercase()
        compareByDescending<FullCoin> {
            it.coin.code.lowercase() == lowercasedFilter
        }.thenByDescending {
            it.coin.code.lowercase().startsWith(lowercasedFilter)
        }.thenByDescending {
            it.coin.name.lowercase().startsWith(lowercasedFilter)
        }.thenComparing(baseComparator)
    } else {
        baseComparator
    }

    return sortedWith(comparator)
}

val Language.displayNameStringRes: Int
    get() = when (this) {
        Language.English -> R.string.Language_English
        Language.Japanese -> R.string.Language_Japanese
        Language.Korean -> R.string.Language_Korean
        Language.Spanish -> R.string.Language_Spanish
        Language.SimplifiedChinese -> R.string.Language_SimplifiedChinese
        Language.TraditionalChinese -> R.string.Language_TraditionalChinese
        Language.French -> R.string.Language_French
        Language.Italian -> R.string.Language_Italian
        Language.Czech -> R.string.Language_Czech
        Language.Portuguese -> R.string.Language_Portuguese
    }

// ImageView

fun ImageView.setRemoteImage(url: String, placeholder: Int? = R.drawable.ic_placeholder) {
    load(url) {
        if (placeholder != null) {
            error(placeholder)
        }
    }
}

fun ImageView.setImage(imageSource: ImageSource) {
    when (imageSource) {
        is ImageSource.Local -> setImageResource(imageSource.resId)
        is ImageSource.Remote -> setRemoteImage(imageSource.url, imageSource.placeholder)
    }
}

// String

fun String.hexToByteArray(): ByteArray {
    return ByteArray(this.length / 2) {
        this.substring(it * 2, it * 2 + 2).toInt(16).toByte()
    }
}

// ByteArray

fun ByteArray.toRawHexString(): String {
    return this.joinToString(separator = "") {
        it.toInt().and(0xff).toString(16).padStart(2, '0')
    }
}

fun ByteArray?.toHexString(): String {
    val rawHex = this?.toRawHexString() ?: return ""
    return "0x$rawHex"
}

// Intent & Parcelable Enum
fun Intent.putParcelableExtra(key: String, value: Parcelable) {
    putExtra(key, value)
}

fun LockTimeInterval?.stringResId(): Int {
    return when (this) {
        LockTimeInterval.hour -> R.string.Send_LockTime_Hour
        LockTimeInterval.month -> R.string.Send_LockTime_Month
        LockTimeInterval.halfYear -> R.string.Send_LockTime_HalfYear
        LockTimeInterval.year -> R.string.Send_LockTime_Year
        null -> R.string.Send_LockTime_Off
    }
}

fun String.shorten(): String {
    val prefixes = listOf("0x", "bc", "bnb", "ltc", "bitcoincash:", "ecash:")

    var prefix = ""
    for (p in prefixes) {
        if (this.startsWith(p)) {
            prefix = p
            break
        }
    }

    val withoutPrefix = this.removePrefix(prefix)

    val characters = 4
    return if (withoutPrefix.length > characters * 2)
        prefix + withoutPrefix.take(characters) + "..." + withoutPrefix.takeLast(characters)
    else
        this
}

//Compose Animated Navigation

fun NavGraphBuilder.composablePage(
    route: String,
    arguments: List<NamedNavArgument> = emptyList(),
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit,
) {
    composable(
        route = route,
        arguments = arguments,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Left,
                animationSpec = tween(300)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Right,
                animationSpec = tween(300)
            )
        },
        popEnterTransition = { null },
        content = content
    )
}

fun NavGraphBuilder.composablePopup(
    route: String,
    content: @Composable AnimatedVisibilityScope.(NavBackStackEntry) -> Unit
) {
    composable(
        route,
        enterTransition = {
            slideIntoContainer(
                AnimatedContentTransitionScope.SlideDirection.Up,
                animationSpec = tween(250)
            )
        },
        popExitTransition = {
            slideOutOfContainer(
                AnimatedContentTransitionScope.SlideDirection.Down,
                animationSpec = tween(250)
            )
        },
        content = content
    )
}

suspend fun <T> retryWhen(
    times: Int,
    predicate: suspend (cause: Throwable) -> Boolean,
    block: suspend () -> T
): T {
    repeat(times - 1) {
        try {
            return block()
        } catch (e: Throwable) {
            if (!predicate(e)) {
                throw e
            }
        }
        delay(1000)
    }
    return block()
}

fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

fun getWalletAddress(account: Account): String? {
    val evmAddress: String? = when (val accountType = account.type) {
        is AccountType.Mnemonic -> {
            val chain = evmBlockchainManager.getChain(BlockchainType.Ethereum)
            Signer.address(accountType.words, accountType.passphrase, chain).eip55
        }

        is AccountType.EvmPrivateKey -> {
            Signer.address(accountType.key).eip55
        }

        is AccountType.EvmAddress -> accountType.address
        is AccountType.SolanaAddress -> accountType.address
        is AccountType.TronAddress -> accountType.address
        is AccountType.TonAddress -> accountType.address
        is AccountType.BitcoinAddress -> accountType.address
        else -> null
    }

    val hdExtendedKey = (account.type as? AccountType.HdExtendedKey)?.hdExtendedKey
    var accountPublicKey = AccountPublicKey(false)

    val publicKey = if (account.type is AccountType.Mnemonic) {
        accountPublicKey = AccountPublicKey(true)
        val seed = Mnemonic().toSeed(account.type.words, account.type.passphrase)
        HDExtendedKey(seed, HDWallet.Purpose.BIP44)
    } else if (hdExtendedKey?.derivedType == HDExtendedKey.DerivedType.Master) {
        accountPublicKey = AccountPublicKey(true)
        hdExtendedKey
    } else if (hdExtendedKey?.derivedType == HDExtendedKey.DerivedType.Account && !hdExtendedKey.isPublic) {
        hdExtendedKey
    } else if (hdExtendedKey?.derivedType == HDExtendedKey.DerivedType.Account && hdExtendedKey.isPublic) {
        hdExtendedKey
    } else {
        null
    }

    val extendedPublicKey = publicKey?.let { ExtendedPublicKey(it, accountPublicKey) }
    val hdExtendedKeyNew = extendedPublicKey?.hdKey

    evmAddress?.let { return it }
    hdExtendedKeyNew?.let { return it.serialize() }

    return  null
}

fun String.convertToDateWithTime(): String {

    val utcDateTime = OffsetDateTime.parse(this)
    val localDateTime = utcDateTime.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime()

    val formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy HH:mm:ss")
    return localDateTime.format(formatter)
}

fun BigDecimal?.formatTokenValue(ticker: String? = null): String {
    // Handle null values
    this ?: return "-"

    val formattedValue = if (this >= BigDecimal.ONE) {
        // For values >= 1, truncate to 4 decimal places
//        this.setScale(4, RoundingMode.DOWN).stripTrailingZeros().toPlainString()
        if (this.stripTrailingZeros().scale() <= 0) {
            this.toPlainString()
        } else {
            this.setScale(4, RoundingMode.DOWN).stripTrailingZeros().toPlainString()
        }
    } else {
        // For values < 1, ensure 4 non-zero decimals
        this.stripTrailingZeros().toPlainString().let { value ->
            val fractionalPart = value.substringAfter('.', "")
            if (fractionalPart.isEmpty()) return@let "0.0000" // Default if fractional part is empty

            val significantDigits = fractionalPart.filter { it != '0' } // Extract non-zero decimals

            if (significantDigits.isNotEmpty()) {
                // Take only the first 4 non-zero digits
                val firstNonZeroIndex = fractionalPart.indexOfFirst { it != '0' }
                val endIndex = firstNonZeroIndex + significantDigits.take(4).length // Take only available digits up to 4
                value.substring(0, value.indexOf('.') + endIndex + 1)
            } else {
                // Pad with zeros to make up 4 non-zero decimals
                "0." + "0".repeat(4)
            }
        }
    }

    // Append ticker if it's not null
    return if (ticker != null) "$formattedValue $ticker" else formattedValue
}

fun BigDecimal?.formatValue(): BigDecimal {
    // Handle null values
    this ?: return BigDecimal.ZERO

    return if (this >= BigDecimal.ONE) {
        // For values >= 1, check if it's a whole number
        if (this.stripTrailingZeros().scale() <= 0) {
            this.stripTrailingZeros() // Return whole number as is
        } else {
            this.setScale(4, RoundingMode.DOWN) // Truncate to 4 decimal places
        }
    } else {
        // For values < 1, ensure 4 non-zero decimals
        this.stripTrailingZeros().toPlainString().let { value ->
            val fractionalPart = value.substringAfter('.', "")
            if (fractionalPart.isEmpty()) return BigDecimal("0.0000") // Default for no fractional part

            val significantDigits = fractionalPart.filter { it != '0' } // Extract non-zero decimals

            if (significantDigits.isNotEmpty()) {
                // Take only the first 4 non-zero digits
                val firstNonZeroIndex = fractionalPart.indexOfFirst { it != '0' }
                val endIndex = firstNonZeroIndex + significantDigits.take(4).length // Take only available digits up to 4
                BigDecimal(value.substring(0, value.indexOf('.') + endIndex + 1))
            } else {
                // Pad with zeros to make up 4 non-zero decimals
                BigDecimal("0.0000")
            }
        }
    }
}



fun Context.getGreeting(): String {
    val currentTime = LocalTime.now()
    return when {
        currentTime.isAfter(LocalTime.of(4, 59)) && currentTime.isBefore(LocalTime.of(12, 0)) -> getString(R.string.good_morning)
        currentTime.isAfter(LocalTime.of(11, 59)) && currentTime.isBefore(LocalTime.of(18, 0)) -> getString(R.string.good_afternoon)
        else -> getString(R.string.good_evening)
    }
}

fun convertToDecimal(amount: BigDecimal, decimals: Int): BigDecimal {
    return amount.divide(BigDecimal.TEN.pow(decimals))
}