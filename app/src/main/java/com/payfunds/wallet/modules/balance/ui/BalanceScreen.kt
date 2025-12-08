package com.payfunds.wallet.modules.balance.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.payfunds.wallet.entities.AccountType
import com.payfunds.wallet.modules.balance.BalanceAccountsViewModel
import com.payfunds.wallet.modules.balance.BalanceModule
import com.payfunds.wallet.modules.balance.BalanceScreenState
import com.payfunds.wallet.modules.balance.cex.BalanceForAccountCex

@Composable
fun BalanceScreen(navController: NavController) {
    val viewModel = viewModel<BalanceAccountsViewModel>(factory = BalanceModule.AccountsFactory())

    when (val tmpAccount = viewModel.balanceScreenState) {

        BalanceScreenState.NoAccount -> BalanceNoAccount(navController)

        is BalanceScreenState.HasAccount -> when (tmpAccount.accountViewItem.type) {
            is AccountType.Cex -> {
                BalanceForAccountCex(navController, tmpAccount.accountViewItem)
            }
            else -> {
                BalanceForAccount(navController, tmpAccount.accountViewItem)
            }
        }
        else -> {

        }
    }
}