package com.payfunds.wallet.network.response_model.transaction

data class From(
    val ens_domain_name: Any,
    val hash: String,
    val implementation_address: Any,
    val implementation_name: Any,
    val implementations: List<Any>,
    val is_contract: Boolean,
    val is_verified: Boolean,
    val metadata: Any,
    val name: Any,
    val private_tags: List<Any>,
    val public_tags: List<Any>,
    val watchlist_names: List<Any>
)