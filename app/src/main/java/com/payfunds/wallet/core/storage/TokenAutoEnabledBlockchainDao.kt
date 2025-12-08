package com.payfunds.wallet.core.storage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import io.horizontalsystems.marketkit.models.BlockchainType
import com.payfunds.wallet.entities.TokenAutoEnabledBlockchain

@Dao
interface TokenAutoEnabledBlockchainDao {

    @Query("SELECT * FROM TokenAutoEnabledBlockchain WHERE accountId = :accountId AND blockchainType = :blockchainType")
    fun get(accountId: String, blockchainType: BlockchainType): TokenAutoEnabledBlockchain?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(tokenAutoEnabledBlockchain: TokenAutoEnabledBlockchain)

}
