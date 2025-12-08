package com.payfunds.wallet.entities.nft

import io.horizontalsystems.marketkit.models.BlockchainType
import com.payfunds.wallet.entities.Account

data class NftKey(
    val account: Account,
    val blockchainType: BlockchainType
)