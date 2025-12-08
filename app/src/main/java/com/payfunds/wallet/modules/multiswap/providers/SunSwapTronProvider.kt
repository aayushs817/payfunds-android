package com.payfunds.wallet.modules.multiswap.providers

import android.util.Log
import org.bouncycastle.util.encoders.Hex
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.modules.multiswap.ISwapFinalQuote
import com.payfunds.wallet.modules.multiswap.ISwapQuote
import com.payfunds.wallet.modules.multiswap.SwapFinalQuoteTron
import com.payfunds.wallet.modules.multiswap.SwapQuoteTron
import com.payfunds.wallet.modules.multiswap.SwapRouteNotFound
import com.payfunds.wallet.modules.multiswap.providers.tron.TronGridServiceNew
import com.payfunds.wallet.modules.multiswap.providers.tron.TronFeeEstimator
import com.payfunds.wallet.modules.multiswap.providers.tron.TronWallet
import com.payfunds.wallet.modules.multiswap.providers.tron.SwapQuote
import com.payfunds.wallet.modules.multiswap.sendtransaction.SendTransactionData
import com.payfunds.wallet.modules.multiswap.sendtransaction.SendTransactionSettings
import com.payfunds.wallet.modules.multiswap.settings.SwapSettingSlippage
import com.payfunds.wallet.modules.multiswap.ui.DataField
import com.payfunds.wallet.modules.multiswap.ui.DataFieldSlippage
import io.horizontalsystems.marketkit.models.BlockchainType
import io.horizontalsystems.marketkit.models.Token
import io.horizontalsystems.marketkit.models.TokenType
import io.horizontalsystems.tronkit.TronKit
import io.horizontalsystems.tronkit.contracts.ContractMethodHelper
import io.horizontalsystems.tronkit.models.Address
import io.horizontalsystems.tronkit.models.TriggerSmartContract
import io.horizontalsystems.tronkit.network.ApiKeyProvider
import io.horizontalsystems.tronkit.network.Network
import io.horizontalsystems.tronkit.toRawHexString
import java.io.ByteArrayOutputStream
import java.math.BigDecimal
import java.math.BigInteger
import java.math.RoundingMode
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.getOrPut

object SunSwapTronProvider : IMultiSwapProvider {

    override val id: String = "sunswap_tron"
    override val title: String = "SunSwap"
    override val url: String = "https://sunswap.com/"
    override val icon: Int = R.drawable.trx_icon
    override val priority: Int = 50

    private const val DEFAULT_DEADLINE_SECONDS = 20 * 60L
    private const val SUN_SWAP_SMART_ROUTER = "TCFNp179Lg46D16zKoumd4Poa2WFFdtqYj"
    private const val SUN_SWAP_LEGACY_ROUTER = "TKzxdSv2FZKQrEqkKVgp5DcwEXBEKMg2Ax"
    private const val WTRX_BASE58 = "TNUC9Qb1rRpS5CbWLmNMxXBjyFoydXjWFR"
    private const val USDT_BASE58 = "TR7NHqjeKQxGTCi8q8ZY4pL8otSzgjLj6t"
    private const val USDC_BASE58 = "TEkxiTehnzSmSe2XqrBj4w32RUN966rdz8"
    private const val SWAP_EXACT_INPUT_SIGNATURE =
        "swapExactInput(address[],string[],uint256[],uint24[],(uint256,uint256,address,uint256))"

    // Sentinel for native TRX in Smart Router paths (0x00..00 on-chain)
    private const val TRX_NATIVE_SENTINEL = "0"

    // Special config for TRX <-> USDT routes (matches observed successful txs)
    private val TRX_USDT_POOL_VERSION = listOf("v1")
    private val TRX_USDT_VERSION_LEN = listOf(2L)
    private val TRX_USDT_FEES = listOf(0L, 0L)

    private val smartRouterAddress = Address.fromBase58(SUN_SWAP_SMART_ROUTER)
    private val tronGridServices = ConcurrentHashMap<Network, TronGridServiceNew>()

    override fun supports(blockchainType: BlockchainType) = blockchainType == BlockchainType.Tron

    // -------- QUOTE (view only, no tx) ----------

    override suspend fun fetchQuote(
        tokenIn: Token,
        tokenOut: Token,
        amountIn: BigDecimal,
        settings: Map<String, Any?>
    ): ISwapQuote {
        val slippageSetting = SwapSettingSlippage(settings, BigDecimal("1"))

        return withTronKit(tokenIn) { tronKit ->
            val amountInScaled = scaleAmount(amountIn, tokenIn.decimals)

            val path = buildRawPath(tokenIn, tokenOut)

            val decoded = fetchAmountsOut(tronKit, amountInScaled, path)
            if (decoded.isEmpty()) throw SwapRouteNotFound()

            val amountOutRaw = decoded.last()
            if (amountOutRaw == BigInteger.ZERO) throw SwapRouteNotFound()

            val amountOut = scaleAmount(amountOutRaw, tokenOut.decimals)

            val fields = buildList<DataField> {
                slippageSetting.value?.let { add(DataFieldSlippage(it)) }
            }

            SwapQuoteTron(
                amountOut = amountOut,
                priceImpact = null,
                fields = fields,
                settings = listOf(slippageSetting),
                tokenIn = tokenIn,
                tokenOut = tokenOut,
                amountIn = amountIn,
                actionRequired = null
            )
        }
    }

    // -------- FINAL QUOTE (with tx data + network fee) ----------

    override suspend fun fetchFinalQuote(
        tokenIn: Token,
        tokenOut: Token,
        amountIn: BigDecimal,
        swapSettings: Map<String, Any?>,
        sendTransactionSettings: SendTransactionSettings?
    ): ISwapFinalQuote {
        check(sendTransactionSettings is SendTransactionSettings.Tron)

        val slippageSetting = SwapSettingSlippage(swapSettings, BigDecimal("1"))
        val slippage = slippageSetting.valueOrDefault()

        return withTronKit(tokenIn) { tronKit ->
            val amountInScaled = scaleAmount(amountIn, tokenIn.decimals)

            // Raw path for quoting (TRX → WTRX)
            val rawPath = buildRawPath(tokenIn, tokenOut)

            Log.i("SunSwapTron", "rawPath for quote: $rawPath")

            // Quote + energy hint
            val (amounts, energyUsedFromQuote) = fetchAmountsOutWithEnergy(tronKit, amountInScaled, rawPath)
            val amountOutRaw = amounts.lastOrNull() ?: throw SwapRouteNotFound()
            if (amountOutRaw == BigInteger.ZERO) throw SwapRouteNotFound()

            val amountOut = scaleAmount(amountOutRaw, tokenOut.decimals)

            val slippageFraction = slippage.divide(BigDecimal(100), 8, RoundingMode.HALF_UP)

            // UI minOut: what the user sees on confirmation screen
            val userAmountOutMin = amountOut.multiply(BigDecimal.ONE - slippageFraction)
                .max(BigDecimal.ZERO)

            val tokenInIsTrx = tokenIn.type is TokenType.Native
            val tokenOutIsTrx = tokenOut.type is TokenType.Native

            // TX minOut: conservative on-chain minOut to avoid "Too few coins in result"
            // Always 50% of quoted amount for now (safe mode for all pairs)
            val amountOutMinForTx = amountOut
                .multiply(BigDecimal("0.5"))
                .max(BigDecimal.ZERO)

            val amountOutMinScaled = scaleAmount(amountOutMinForTx, tokenOut.decimals)
            val deadlineSeconds = System.currentTimeMillis() / 1000 + DEFAULT_DEADLINE_SECONDS
            val deadline = BigInteger.valueOf(deadlineSeconds)

            // Build Smart Router path (use 0 sentinel where TRX appears)
            val pathBase58 = buildRouterPathBase58(
                tokenInIsTrx = tokenInIsTrx,
                tokenOutIsTrx = tokenOutIsTrx,
                rawPath = rawPath
            )

            // Decide poolVersion/versionLen/fees based on path (TRX-USDT, stable, generic V2)
            val (poolVersion, versionLen, fees) = buildPoolMetadata(pathBase58)

            Log.i("SunSwapTron", "router path=$pathBase58, poolVersion=$poolVersion, versionLen=$versionLen, fees=$fees")

            val contract = buildSwapContract(
                amountInScaled = amountInScaled,
                amountOutMinScaled = amountOutMinScaled,
                pathBase58 = pathBase58,
                poolVersion = poolVersion,
                versionLen = versionLen,
                fees = fees,
                recipient = tronKit.address,
                deadline = deadline,
                tokenInIsTrx = tokenInIsTrx
            )

            // Fee estimation
            val feeEstimator = TronFeeEstimator()
            val tronWallet = object : TronWallet {
                override val addressBase58: String = tronKit.address.base58
                override fun signTronTx(rawDataHex: String): String = ""
            }

            val swapQuoteForFee = SwapQuote(
                path = pathBase58,
                poolVersion = poolVersion,
                versionLen = versionLen,
                fees = fees,
                amountIn = amountInScaled,
                amountOutMin = amountOutMinScaled,
                to = tronKit.address.base58,
                deadline = deadline,
                isTrxInput = tokenInIsTrx
            )

            val feeEstimate = try {
                feeEstimator.estimateSwapFee(
                    wallet = tronWallet,
                    quote = swapQuoteForFee
                ).first
            } catch (error: Throwable) {
                Log.w("SunSwapFee", "Fee estimation fallback: ${error.message}")
                null
            }

            val fields = buildList<DataField> {
                slippageSetting.value?.let { add(DataFieldSlippage(it)) }
            }

            val estimatedFeeTRX = feeEstimate?.energyFeeTrx ?: run {
                val energyUsed = energyUsedFromQuote ?: 0L
                val energyPriceSun = 420L
                BigDecimal(energyUsed * energyPriceSun)
                    .divide(BigDecimal(1_000_000), 6, RoundingMode.HALF_UP)
            }

            Log.i("SunSwapTron", "Estimated swap fee = $estimatedFeeTRX TRX")

            SwapFinalQuoteTron(
                tokenIn = tokenIn,
                tokenOut = tokenOut,
                amountIn = amountIn,
                amountOut = amountOut,
                amountOutMin = userAmountOutMin,  // UI minOut
                sendTransactionData = SendTransactionData.Tron(
                    contract = contract,
                    amountInScaled = amountInScaled,
                    feeEstimate = feeEstimate,
                    feeLimit = feeEstimate?.suggestedFeeLimitSun
                        ?.min(BigInteger.valueOf(Long.MAX_VALUE))
                        ?.toLong()
                ),
                priceImpact = null,
                fields = fields,
                estimationFees = estimatedFeeTRX.toPlainString()
            )
        }
    }

    // TRX → WTRX mapping for quoting
    fun Token.getContractOrNative(): String =
        when (val type = this.type) {
            is TokenType.Eip20 -> Address.fromBase58(type.address).base58
            TokenType.Native -> Address.fromBase58(WTRX_BASE58).base58
            else -> throw IllegalArgumentException("Unsupported token type")
        }

    // -------- Low-level quoting helpers ----------

    private suspend fun fetchAmountsOut(
        tronKit: TronKit,
        amountInScaled: BigInteger,
        path: List<Address>
    ): List<BigInteger> {
        val tronGridService = tronGridService(tronKit.network)
        val functionSelector = "getAmountsOut(uint256,address[])"
        val parameter = encodeGetAmountsOut(amountInScaled, path)

        suspend fun call(router: Address): List<BigInteger> {
            val response = tronGridService.triggerConstantContract(
                ownerAddress = tronKit.address,
                contractAddress = router,
                functionSelector = functionSelector,
                parameter = parameter,
            )
            val constantResult = response.constant_result
            if (constantResult.isNullOrEmpty()) return emptyList()
            val dataBytes = Hex.decode(constantResult[0])
            return decodeUint256ArrayTron(dataBytes)
        }

        return call(smartRouterAddress).takeIf { it.isNotEmpty() }
            ?: call(Address.fromBase58(SUN_SWAP_LEGACY_ROUTER))
    }

    private suspend fun fetchAmountsOutWithEnergy(
        tronKit: TronKit,
        amountInScaled: BigInteger,
        path: List<Address>
    ): Pair<List<BigInteger>, Long?> {
        val tronGridService = tronGridService(tronKit.network)
        val functionSelector = "getAmountsOut(uint256,address[])"
        val parameter = encodeGetAmountsOut(amountInScaled, path)

        suspend fun call(router: Address): Pair<List<BigInteger>, Long?> {
            val response = tronGridService.triggerSmartContract(
                ownerAddress = tronKit.address,
                contractAddress = router,
                functionSelector = functionSelector,
                parameter = parameter,
                callValue = 0L,
                feeLimit = 30_000_000L,
                visible = true,
                estimateEnergy = true
            )
            val constantResult = response.constant_result
            if (constantResult.isNullOrEmpty()) return emptyList<BigInteger>() to null
            val dataBytes = Hex.decode(constantResult[0])
            val amounts = decodeUint256ArrayTron(dataBytes)
            val energyUsed = response.energy_used?.toLong()
            return amounts to energyUsed
        }

        val first = call(smartRouterAddress)
        if (first.first.isNotEmpty()) return first
        return call(Address.fromBase58(SUN_SWAP_LEGACY_ROUTER))
    }

    private suspend fun <T> withTronKit(token: Token, block: suspend (TronKit) -> T): T {
        val wallet = App.Companion.walletManager.activeWallets.firstOrNull { it.token == token }
            ?: throw IllegalStateException("Wallet not found for token ${token.coin.code}")

        val tronKitWrapper = App.Companion.tronKitManager.getTronKitWrapper(wallet.account)
        try {
            return block(tronKitWrapper.tronKit)
        } finally {
            App.Companion.tronKitManager.unlink(wallet.account)
        }
    }

    private fun tronGridService(network: Network): TronGridServiceNew {
        return tronGridServices.getOrPut(network) {
            TronGridServiceNew(
                network,
                ApiKeyProvider(App.Companion.appConfigProvider.trongridApiKeys)
            )
        }
    }

    fun encodeGetAmountsOut(
        amountIn: BigInteger,
        path: List<Address>
    ): String {
        val amountHex = amountIn.toString(16).padStart(64, '0')
        val arrayOffset =
            "0000000000000000000000000000000000000000000000000000000000000040"
        val lengthHex = path.size.toString(16).padStart(64, '0')

        val addressesHex = buildString {
            path.forEach { addr ->
                val word = encodeAddress(addr.base58)
                append(word.toHex())
            }
        }

        return amountHex + arrayOffset + lengthHex + addressesHex
    }

    // -------- Contract build (router call) ----------

    private fun buildSwapContract(
        amountInScaled: BigInteger,
        amountOutMinScaled: BigInteger,
        pathBase58: List<String>,
        poolVersion: List<String>,
        versionLen: List<Long>,
        fees: List<Long>,
        recipient: Address,
        deadline: BigInteger,
        tokenInIsTrx: Boolean
    ): TriggerSmartContract {
        val swapData = SwapData(
            amountIn = amountInScaled,
            amountOutMin = amountOutMinScaled,
            to = recipient.base58,
            deadline = deadline
        )

        val encodedArgs = encodeSwapExactInputArgs(
            path = pathBase58,
            poolVersion = poolVersion,
            versionLen = versionLen,
            fees = fees,
            swapData = swapData
        )

        val methodId = ContractMethodHelper
            .getMethodId(SWAP_EXACT_INPUT_SIGNATURE)
            .toRawHexString()

        val data = (methodId + encodedArgs).lowercase()

        Log.i("SunSwapTron", "swapExactInput data=$data")

        return TriggerSmartContract(
            data = data,
            ownerAddress = recipient,
            contractAddress = smartRouterAddress,
            callValue = if (tokenInIsTrx) amountInScaled else BigInteger.ZERO,
            callTokenValue = null,
            tokenId = null,
            functionSelector = SWAP_EXACT_INPUT_SIGNATURE,
            parameter = encodedArgs
        )
    }

    // -------- Generic helpers ----------

    private fun scaleAmount(amount: BigDecimal, decimals: Int): BigInteger {
        val scaled = amount.movePointRight(decimals)
        return scaled.setScale(0, RoundingMode.DOWN).toBigInteger()
    }

    private fun scaleAmount(amount: BigInteger, decimals: Int): BigDecimal {
        return amount.toBigDecimal().movePointLeft(decimals).stripTrailingZeros()
    }

    private fun decodeUint256ArrayTron(data: ByteArray): List<BigInteger> {
        if (data.isEmpty() || data.size < 64) return emptyList()

        val offset = BigInteger(1, data.copyOfRange(0, 32)).toInt()

        if (data.size < offset + 32) return emptyList()
        val length = BigInteger(1, data.copyOfRange(offset, offset + 32)).toInt()

        val result = mutableListOf<BigInteger>()
        var index = offset + 32

        repeat(length) {
            if (index + 32 > data.size) return@repeat
            val value = BigInteger(1, data.copyOfRange(index, index + 32))
            result.add(value)
            index += 32
        }

        return result
    }

    private data class SwapData(
        val amountIn: BigInteger,
        val amountOutMin: BigInteger,
        val to: String,
        val deadline: BigInteger
    )

    // ---- Route metadata helpers (TRX<->USDT, stable, generic V2) ----

    private fun isStableUsdtUsdc(pathBase58: List<String>): Boolean {
        return pathBase58.size == 2 && (
                (pathBase58[0] == USDT_BASE58 && pathBase58[1] == USDC_BASE58) ||
                        (pathBase58[0] == USDC_BASE58 && pathBase58[1] == USDT_BASE58)
                )
    }

    private fun isTrxUsdtRoute(pathBase58: List<String>): Boolean {
        if (pathBase58.size != 2) return false
        val s = pathBase58.toSet()
        return s.contains(TRX_NATIVE_SENTINEL) && s.contains(USDT_BASE58)
    }

    private fun buildRouterPathBase58(
        tokenInIsTrx: Boolean,
        tokenOutIsTrx: Boolean,
        rawPath: List<Address>
    ): List<String> {
        return when {
            tokenInIsTrx && tokenOutIsTrx -> {
                // TRX -> TRX is a no-op, but keep a sane placeholder
                listOf(TRX_NATIVE_SENTINEL)
            }

            tokenInIsTrx -> {
                // rawPath[0] = WTRX, rawPath[1] = tokenOut
                listOf(TRX_NATIVE_SENTINEL) + rawPath.drop(1).map { it.base58 }
            }

            tokenOutIsTrx -> {
                // rawPath[last] = WTRX
                val baseTokens = rawPath.dropLast(1).map { it.base58 }
                baseTokens + TRX_NATIVE_SENTINEL
            }

            else -> rawPath.map { it.base58 }
        }
    }

    private fun buildPoolMetadata(
        pathBase58: List<String>
    ): Triple<List<String>, List<Long>, List<Long>> {
        // 1) TRX <-> USDT special case (matches on-chain tx)
        if (isTrxUsdtRoute(pathBase58)) {
            return Triple(TRX_USDT_POOL_VERSION, TRX_USDT_VERSION_LEN, TRX_USDT_FEES)
        }

        // 2) Stable USDT/USDC pool
        if (isStableUsdtUsdc(pathBase58)) {
            return Triple(
                listOf("usdc2pooltusdusdt"),
                listOf(2L),
                listOf(0L, 0L)
            )
        }

        // 3) Generic V2-style single-hop route for all other token pairs
        val hopsCount = (pathBase58.size - 1).coerceAtLeast(1)
        return Triple(
            listOf("V2"),
            listOf(hopsCount.toLong()),
            List(hopsCount) { 0L }
        )
    }

    // -------- ABI encoding helpers ----------

    private fun encodeSwapExactInputArgs(
        path: List<String>,
        poolVersion: List<String>,
        versionLen: List<Long>,
        fees: List<Long>,
        swapData: SwapData
    ): String {
        val pathEncoded = encodeAddressArray(path)
        val poolVersionsEncoded = encodeStringArray(poolVersion)
        val versionLenEncoded = encodeUintArray(versionLen.map { BigInteger.valueOf(it) })
        val feesEncoded = encodeUintArray(fees.map { BigInteger.valueOf(it) })
        val swapDataEncoded = encodeSwapDataTuple(swapData)

        val headSize = (32 * 4) + swapDataEncoded.size
        var dynamicOffset = headSize

        val headParts = mutableListOf<ByteArray>()
        headParts += encodeUint(BigInteger.valueOf(dynamicOffset.toLong()))
        dynamicOffset += pathEncoded.size

        headParts += encodeUint(BigInteger.valueOf(dynamicOffset.toLong()))
        dynamicOffset += poolVersionsEncoded.size

        headParts += encodeUint(BigInteger.valueOf(dynamicOffset.toLong()))
        dynamicOffset += versionLenEncoded.size

        headParts += encodeUint(BigInteger.valueOf(dynamicOffset.toLong()))
        dynamicOffset += feesEncoded.size

        headParts += swapDataEncoded

        val buffer = ByteArrayOutputStream()
        headParts.forEach { buffer.write(it) }
        buffer.write(pathEncoded)
        buffer.write(poolVersionsEncoded)
        buffer.write(versionLenEncoded)
        buffer.write(feesEncoded)

        return buffer.toByteArray().toHex()
    }

    private fun encodeSwapDataTuple(data: SwapData): ByteArray {
        val buffer = ByteArrayOutputStream()
        buffer.write(encodeUint(data.amountIn))
        buffer.write(encodeUint(data.amountOutMin))
        buffer.write(encodeAddress(data.to))
        buffer.write(encodeUint(data.deadline))
        return buffer.toByteArray()
    }

    private fun encodeAddressArray(values: List<String>): ByteArray {
        val buffer = ByteArrayOutputStream()
        buffer.write(encodeUint(BigInteger.valueOf(values.size.toLong())))
        values.forEach { address ->
            buffer.write(encodeAddress(address))
        }
        return buffer.toByteArray()
    }

    private fun encodeUintArray(values: List<BigInteger>): ByteArray {
        val buffer = ByteArrayOutputStream()
        buffer.write(encodeUint(BigInteger.valueOf(values.size.toLong())))
        values.forEach { value -> buffer.write(encodeUint(value)) }
        return buffer.toByteArray()
    }

    private fun encodeStringArray(values: List<String>): ByteArray {
        val heads = mutableListOf<ByteArray>()
        val tails = mutableListOf<ByteArray>()

        // first string offset: 32 * values.size (no extra +32)
        var offset = 32 * values.size

        values.forEach { value ->
            val encodedString = encodeDynamicString(value)
            heads += encodeUint(BigInteger.valueOf(offset.toLong()))
            tails += encodedString
            offset += encodedString.size
        }

        val buffer = ByteArrayOutputStream()
        buffer.write(encodeUint(BigInteger.valueOf(values.size.toLong())))
        heads.forEach { buffer.write(it) }
        tails.forEach { buffer.write(it) }
        return buffer.toByteArray()
    }

    private fun encodeDynamicString(value: String): ByteArray {
        val data = value.toByteArray(Charsets.UTF_8)
        val buffer = ByteArrayOutputStream()
        buffer.write(encodeUint(BigInteger.valueOf(data.size.toLong())))
        buffer.write(padRightToWord(data))
        return buffer.toByteArray()
    }

    private fun encodeAddress(address: String): ByteArray {
        // TRX sentinel “0” is encoded as 32 bytes of zero
        if (address == TRX_NATIVE_SENTINEL || address == "0x0") {
            return ByteArray(32) { 0 }
        }

        val tronAddress = Address.fromBase58(address)
        val fullHex = tronAddress.hex.removePrefix("0x") // e.g. "41a614f8..."

        // Strip 0x41 tron prefix → keep only 20-byte EVM address
        val evmHex = when {
            fullHex.length == 42 && fullHex.startsWith("41", ignoreCase = true) ->
                fullHex.substring(2)
            fullHex.length > 40 ->
                fullHex.substring(fullHex.length - 40)
            else ->
                fullHex.padStart(40, '0')
        }

        return padLeftToWord(hexStringToByteArray(evmHex))
    }

    private fun buildRawPath(tokenIn: Token, tokenOut: Token): List<Address> {
        val makesTrxToUsdcHop = (tokenIn.type is TokenType.Native && tokenOut.isUsdc())
        val makesUsdcToTrxHop = (tokenOut.type is TokenType.Native && tokenIn.isUsdc())

        val segments = mutableListOf<String>()
        segments += tokenIn.getContractOrNative()

        if (makesTrxToUsdcHop || makesUsdcToTrxHop) {
            segments += USDT_BASE58
        }

        segments += tokenOut.getContractOrNative()

        return segments.map { Address.fromBase58(it) }
    }

    private fun Token.isUsdc(): Boolean {
        val type = this.type as? TokenType.Eip20 ?: return false
        return type.address.equals(USDC_BASE58, ignoreCase = true)
    }

    private fun encodeUint(value: BigInteger): ByteArray {
        val normalized = value.toByteArray().let {
            if (it.size > 32) it.copyOfRange(it.size - 32, it.size) else it
        }
        return padLeftToWord(normalized)
    }

    private fun padLeftToWord(data: ByteArray): ByteArray {
        val result = ByteArray(32)
        val copyFrom = data.size.coerceAtMost(32)
        System.arraycopy(data, data.size - copyFrom, result, 32 - copyFrom, copyFrom)
        return result
    }

    private fun padRightToWord(data: ByteArray): ByteArray {
        val paddedSize = ((data.size + 31) / 32) * 32
        val result = ByteArray(paddedSize)
        System.arraycopy(data, 0, result, 0, data.size)
        return result
    }

    private fun hexStringToByteArray(hex: String): ByteArray {
        var cleanHex = hex.removePrefix("0x")
        if (cleanHex.length % 2 != 0) {
            cleanHex = "0$cleanHex"
        }
        val data = ByteArray(cleanHex.length / 2)
        for (i in data.indices) {
            val index = i * 2
            data[i] = ((cleanHex[index].digitToInt(16) shl 4) +
                    cleanHex[index + 1].digitToInt(16)).toByte()
        }
        return data
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }

    private fun parseAddress(raw: String): Address {
        return try {
            Address.fromBase58(raw)
        } catch (_: Exception) {
            val normalized = raw.removePrefix("0x")
            val withPrefix = if (normalized.startsWith("41", true)) normalized else "41$normalized"
            Address.fromHex(withPrefix)
        }
    }
}
