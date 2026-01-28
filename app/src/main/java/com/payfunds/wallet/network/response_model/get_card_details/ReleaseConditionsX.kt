package com.payfunds.wallet.network.response_model.get_card_details

data class ReleaseConditionsX(
    val escrow: Escrow,
    val kyb: Kyb,
    val kyc: Kyc,
    val minimumBalance: MinimumBalance,
    val quantityPerUser: Int,
    val totalDepositVolume: TotalDepositVolume,
    val typeOfUser: TypeOfUser
)