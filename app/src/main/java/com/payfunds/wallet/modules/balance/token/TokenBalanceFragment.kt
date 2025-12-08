package com.payfunds.wallet.modules.balance.token

import android.widget.Toast
import androidx.compose.runtime.Composable
import androidx.fragment.app.viewModels
import androidx.navigation.NavController
import androidx.navigation.navGraphViewModels
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.getInput
import com.payfunds.wallet.entities.Wallet
import com.payfunds.wallet.modules.transactions.TransactionsModule
import com.payfunds.wallet.modules.transactions.TransactionsViewModel

class TokenBalanceFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        val wallet = navController.getInput<Wallet>()
        if (wallet == null) {
            Toast.makeText(App.instance, "Wallet is Null", Toast.LENGTH_SHORT).show()
            navController.popBackStack(R.id.tokenBalanceFragment, true)
            return
        }
        val viewModel by viewModels<TokenBalanceViewModel> { TokenBalanceModule.Factory(wallet) }
        val transactionsViewModel by navGraphViewModels<TransactionsViewModel>(R.id.mainFragment) { TransactionsModule.Factory() }

        TokenBalanceScreen(
            viewModel,
            transactionsViewModel,
            navController
        )
    }

}
