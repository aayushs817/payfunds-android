package com.payfunds.wallet.network.response_model.transaction

data class To(
    val ens_domain_name: Any,
    val hash: String,
    val implementation_address: Any,
    val implementation_name: Any,
    val implementations: List<Any>,
    val is_contract: Boolean,
    val is_verified: Boolean,
    val metadata: Any,
    val name: String? = null,
    val private_tags: List<Any>,
    val public_tags: List<Any>,
    val watchlist_names: List<Any>
)