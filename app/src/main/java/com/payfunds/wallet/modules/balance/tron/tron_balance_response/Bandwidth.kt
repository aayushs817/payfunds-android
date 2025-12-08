package com.payfunds.wallet.modules.balance.tron.tron_balance_response

data class Bandwidth(
    val assets: Assets,
    val energyLimit: Int,
    val energyPercentage: Int,
    val energyRemaining: Int,
    val energyUsed: Int,
    val freeNetLimit: Int,
    val freeNetPercentage: Int,
    val freeNetRemaining: Int,
    val freeNetUsed: Int,
    val netLimit: Int,
    val netPercentage: Int,
    val netRemaining: Int,
    val netUsed: Int,
    val storageLimit: Int,
    val storagePercentage: Int,
    val storageRemaining: Int,
    val storageUsed: Int,
    val totalEnergyLimit: Int,
    val totalEnergyWeight: Int,
    val totalNetLimit: Int,
    val totalNetWeight: Int
)