package com.payfunds.wallet.modules.transactions

import com.payfunds.wallet.entities.TransactionValue
import com.payfunds.wallet.entities.transactionrecords.evm.EvmTransactionRecord
import com.payfunds.wallet.entities.transactionrecords.tron.TronContractCallTransactionRecord

object TronSwapClassifier {
    private val methodKeywords = listOf("swap", "exchange", "exact", "output", "input")
    private val routerAddresses = setOf(
        "TCFNp179Lg46D16zKoumd4Poa2WFFdtqYj", // SunSwap smart router
        "TKzxdSv2FZKQrEqkKVgp5DcwEXBEKMg2Ax"  // SunSwap legacy router
    )

    data class Result(
        val valueOut: TransactionValue?,
        val valueIn: TransactionValue?
    )

    fun classify(record: TronContractCallTransactionRecord): Result? {
        val (incomingValues, outgoingValues) = EvmTransactionRecord.combined(
            record.incomingEvents,
            record.outgoingEvents
        )

        // Filter out zero values
        val nonZeroIncoming = incomingValues.filter { !it.zeroValue }
        val nonZeroOutgoing = outgoingValues.filter { !it.zeroValue }

        // Need at least one incoming OR one outgoing value
        if (nonZeroIncoming.isEmpty() && nonZeroOutgoing.isEmpty()) return null

        val valueOut = if (nonZeroIncoming.isNotEmpty()) {
             if (nonZeroIncoming.size > 1) {
                nonZeroIncoming.firstOrNull { incoming ->
                    incoming.coinUid.isNotEmpty() || incoming.coinCode.isNotEmpty()
                } ?: nonZeroIncoming.first()
            } else {
                nonZeroIncoming.first()
            }
        } else null

        val valueIn = if (nonZeroOutgoing.isNotEmpty()) {
             if (nonZeroOutgoing.size > 1) {
                // If multiple outgoing values, prefer non-native token (TRC20) over native TRX
                nonZeroOutgoing.firstOrNull { outgoing ->
                    // Logic to avoid picking same token if possible, though valueOut might be null now
                    if (valueOut != null) {
                         when {
                            valueOut.coinUid.isNotEmpty() && outgoing.coinUid.isNotEmpty() ->
                                outgoing.coinUid != valueOut.coinUid
                            else ->
                                outgoing.coinCode.isNotEmpty() && valueOut.coinCode.isNotEmpty() &&
                                outgoing.coinCode != valueOut.coinCode
                        }
                    } else true
                } ?: nonZeroOutgoing.first()
            } else {
                nonZeroOutgoing.first()
            }
        } else null

        // Check if tokens are different by comparing both coinUid and coinCode
        val areDifferentTokens = if (valueOut != null && valueIn != null) {
            when {
                // If both have coinUid, compare by coinUid
                valueOut.coinUid.isNotEmpty() && valueIn.coinUid.isNotEmpty() ->
                    valueOut.coinUid != valueIn.coinUid
                // Otherwise, compare by coinCode (works for TokenValue and when coinUid is empty)
                else ->
                    valueOut.coinCode.isNotEmpty() && valueIn.coinCode.isNotEmpty() &&
                    valueOut.coinCode != valueIn.coinCode
            }
        } else {
            // If one is missing, we can't compare, but it "could" be a swap if method match is strong
            true
        }

        // Check if this looks like a swap transaction:
        // 1. Has at least one incoming or outgoing value (checked above)
        // 2. The values are different tokens (if both exist)
        // 3. For strict pattern matching, prefer both to exist
        val hasSwapPattern = areDifferentTokens && 
                            nonZeroIncoming.isNotEmpty() && 
                            nonZeroOutgoing.isNotEmpty()

        // Check method name or contract address
        val methodMatches = record.method
            ?.lowercase()
            ?.let { method -> methodKeywords.any { keyword -> method.contains(keyword) } }
            ?: false
        val contractMatches = routerAddresses.contains(record.contractAddress)

        // Classify as swap if:
        // A) It has a full swap pattern (In + Out + Diff Tokens) -> Strongest signal
        // B) It has partial pattern (In OR Out) AND (Method Match OR Contract Match) -> Partial signal
        
        if (hasSwapPattern) {
             return Result(
                valueOut = valueOut,
                valueIn = valueIn
            )
        }
        
        if ((nonZeroIncoming.isNotEmpty() || nonZeroOutgoing.isNotEmpty()) && (methodMatches || contractMatches)) {
             return Result(
                valueOut = valueOut,
                valueIn = valueIn
            )
        }

        return null
    }
}

