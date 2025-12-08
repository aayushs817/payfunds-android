package com.payfunds.wallet.modules.depositcex

import androidx.lifecycle.ViewModel
import com.payfunds.wallet.core.providers.CexAsset
import com.payfunds.wallet.core.providers.CexDepositNetwork
import com.payfunds.wallet.modules.receive.ui.UsedAddressesParams

class CexDepositSharedViewModel : ViewModel() {

    var network: CexDepositNetwork? = null
    var cexAsset: CexAsset? = null
    var usedAddressesParams: UsedAddressesParams? = null

}