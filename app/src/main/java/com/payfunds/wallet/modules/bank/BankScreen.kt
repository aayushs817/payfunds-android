package com.payfunds.wallet.modules.bank

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.text.style.TextAlign
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.App
import com.payfunds.wallet.core.providers.Translator
import com.payfunds.wallet.core.slideFromRight
import com.payfunds.wallet.entities.ViewState
import com.payfunds.wallet.modules.send.SendFragment
import com.payfunds.wallet.modules.sendtokenselect.PrefilledData
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.*
import io.horizontalsystems.marketkit.models.BlockchainType
import java.text.SimpleDateFormat
import java.util.*
import io.payfunds.core.helpers.DateHelper
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import io.payfunds.core.helpers.HudHelper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import io.payfunds.core.helpers.DateHelper.formatDate

@Composable
fun BankScreen(
    navController: NavController,
    viewModel: BankViewModel = viewModel(factory = BankModule.Factory()),
) {
    val view = LocalView.current
    var showEnableTokenDialog by remember { mutableStateOf(false) }
    var showWithdrawDialog by remember { mutableStateOf(false) }
    var withdrawAmount by remember { mutableStateOf("") }
    var isWithdrawing by remember { mutableStateOf(false) }
    var isDepositing by remember { mutableStateOf(false) }
    val uiState = viewModel.uiState
    val userDetails = viewModel.userDetails

    LaunchedEffect(Unit) {
        viewModel.fetchUserDetails()
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        viewModel.fetchCardHtml()
    }

    val refreshDetails = navController.currentBackStackEntry
        ?.savedStateHandle
        ?.getLiveData<Boolean>("refreshBankDetails")
        ?.observeAsState()

    LaunchedEffect(refreshDetails?.value) {
        if (refreshDetails?.value == true) {
            viewModel.fetchUserDetails()
            navController.currentBackStackEntry?.savedStateHandle?.set("refreshBankDetails", false)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ComposeAppTheme.colors.tyler)
    ) {
        AppBar(
            title = stringResource(R.string.Bank_MyCards),
        )

        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Card Section
            val cardModifier = Modifier
                .padding(horizontal = 16.dp)
                .height(280.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(ComposeAppTheme.colors.lawrence)

            when (uiState) {
                is ViewState.Loading -> {
                    Box(modifier = cardModifier, contentAlignment = Alignment.Center) {
                        HSCircularProgressIndicator()
                    }
                }

                is ViewState.Error,
                is ViewState.Success -> {
                    val bankDetails = userDetails?.data?.bankDetails
                    if (bankDetails == null) {
                        GenericActionCard(
                            modifier = cardModifier,
                            title = stringResource(R.string.Bank_CreateCard),
                            subtitle = stringResource(R.string.Bank_CreateCardSubtitle),
                            icon = R.drawable.ic_plus_20,
                            iconBackground = ComposeAppTheme.colors.jacob,
                            onClick = {
                                val initialStep =
                                    if (bankDetails?.userReferenceId?.isNotEmpty() == true) 2 else 1
                                navController.slideFromRight(
                                    R.id.createCardFragment,
                                    CreateBankUserFragment.Input(initialStep)
                                )
                            }

                        )
                    } else {
                        when {
                            bankDetails.kycStatus.equals("approved", ignoreCase = true) -> {
                                when {
                                    viewModel.cardDetails != null -> {
                                        val card = viewModel.cardDetails!!
                                        CreditCardView(
                                            cardNumber = card.panText,
                                            expiryDate = card.expiry,
                                            cvv = card.cvvText,
                                        )
                                    }
                                    bankDetails.cardRequests -> {
                                        GenericActionCard(
                                            modifier = cardModifier,
                                            title = "Card In Review",
                                            subtitle = "Your card request is being processed",
                                            icon = R.drawable.ic_list_24,
                                            iconBackground = ComposeAppTheme.colors.grey,
                                            onClick = {}
                                        )
                                    }
                                    bankDetails.cards.isEmpty() -> {
                                        GenericActionCard(
                                            modifier = cardModifier,
                                            title = "KYC Approved",
                                            subtitle = "Your bank account is ready",
                                            icon = R.drawable.icon_20_check_1,
                                            iconBackground = ComposeAppTheme.colors.remus,
                                            buttonTitle = "Request card",
                                            onClick = {
                                                viewModel.requestCard()
                                            }
                                        )
                                    }
                                    else -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(12.dp)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            HSCircularProgressIndicator()
                                        }
                                    }
                                }
                            }

                            bankDetails.kycStatus.equals("pending", ignoreCase = true) -> {
                                GenericActionCard(
                                    modifier = cardModifier,
                                    title = "KYC Pending",
                                    subtitle = "Complete your verification to continue",
                                    icon = R.drawable.ic_plus_20,
                                    iconBackground = ComposeAppTheme.colors.jacob,
                                    buttonTitle = "Do KYC",
                                    onClick = {
                                        val initialStep =
                                            if (bankDetails.userReferenceId?.isNotEmpty() == true) 2 else 1
                                        navController.slideFromRight(
                                            R.id.createCardFragment,
                                            CreateBankUserFragment.Input(initialStep)
                                        )
                                    }
                                )
                            }

                            bankDetails.kycStatus.equals("rejected", ignoreCase = true) -> {
                                GenericActionCard(
                                    modifier = cardModifier,
                                    title = "KYC Rejected",
                                    subtitle = bankDetails.rejectionReason ?: "Your verification was not successful",
                                    icon = R.drawable.icon_24_warning_2,
                                    iconBackground = ComposeAppTheme.colors.lucian,
                                    buttonTitle = "Retry KYC",
                                    onClick = {
                                        val initialStep =
                                            if (bankDetails.userReferenceId?.isNotEmpty() == true) 2 else 1
                                        navController.slideFromRight(
                                            R.id.createCardFragment,
                                            CreateBankUserFragment.Input(initialStep)
                                        )
                                    }

                                )
                            }

                            bankDetails.kycStatus.equals("update_required", ignoreCase = true) -> {
                                GenericActionCard(
                                    modifier = cardModifier,
                                    title = "Update Required",
                                    subtitle = bankDetails.updateReason ?: "Additional information needed to proceed",
                                    icon = R.drawable.ic_edit_24,
                                    iconBackground = ComposeAppTheme.colors.jacob,
                                    buttonTitle = "Update KYC",
                                    onClick = {
                                        val initialStep =
                                            if (bankDetails.userReferenceId?.isNotEmpty() == true) 2 else 1
                                        navController.slideFromRight(
                                            R.id.createCardFragment,
                                            CreateBankUserFragment.Input(initialStep)
                                        )
                                    }
                                )
                            }

                            bankDetails.kycStatus.equals("forbidden", ignoreCase = true) -> {
                                GenericActionCard(
                                    modifier = cardModifier,
                                    title = "Access Forbidden",
                                    subtitle = bankDetails.rejectionReason ?: "Your account access is restricted",
                                    icon = R.drawable.icon_24_warning_2,
                                    iconBackground = ComposeAppTheme.colors.lucian,
                                    onClick = {}
                                )
                            }

                            bankDetails.kycStatus.equals("submitted", ignoreCase = true) ||
                                    bankDetails.kycStatus.equals(
                                        "in_review",
                                        ignoreCase = true
                                    ) -> {
                                GenericActionCard(
                                    modifier = cardModifier,
                                    title = "In Review",
                                    subtitle = "We are reviewing your documents",
                                    icon = R.drawable.ic_list_24,
                                    iconBackground = ComposeAppTheme.colors.grey,
                                    onClick = {}
                                )
                            }

                            else -> {
                                GenericActionCard(
                                    modifier = cardModifier,
                                    title = bankDetails.kycStatus.replaceFirstChar { it.uppercase() },
                                    subtitle = "Status: ${bankDetails.kycStatus}",
                                    icon = R.drawable.ic_bank_24,
                                    iconBackground = ComposeAppTheme.colors.jacob,
                                    onClick = {
                                        val initialStep =
                                            if (bankDetails.userReferenceId?.isNotEmpty() == true) 2 else 1
                                        navController.slideFromRight(
                                            R.id.createCardFragment,
                                            CreateBankUserFragment.Input(initialStep)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Action Grid
            val bankDetails = userDetails?.data?.bankDetails
            val isKycApproved = bankDetails?.kycStatus.equals("approved", ignoreCase = true) == true
            val hasCard = bankDetails?.cards?.isNotEmpty() == true
            val areBankActionsEnabled = isKycApproved && hasCard

            // Card Balance Section
            val cardBalanceData = viewModel.cardBalance?.data
            if (cardBalanceData != null) {
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(ComposeAppTheme.colors.lawrence)
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Account Balance",
                        style = ComposeAppTheme.typography.body,
                        color = ComposeAppTheme.colors.leah
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${cardBalanceData.currency} ${String.format("%.2f", cardBalanceData.balance)}",
                        style = ComposeAppTheme.typography.body,
                        color = ComposeAppTheme.colors.leah
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionItem(
                    text = stringResource(R.string.Bank_CardControl),
                    iconRes = R.drawable.ic_bank_24,
                    modifier = Modifier.weight(1f),
                    enabled = hasCard,
                    onClick = {
                        navController.slideFromRight(R.id.cardControlFragment)
                    }
                )
                ActionItem(
                    text = stringResource(R.string.Transactions_Title),
                    iconRes = R.drawable.ic_list_24,
                    modifier = Modifier.weight(1f),
                    enabled = areBankActionsEnabled,
                    onClick = {
                        navController.slideFromRight(R.id.transactionsFragment)
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ActionItem(
                    text = stringResource(R.string.Bank_Deposit),
                    iconRes = R.drawable.ic_arrow_medium2_down_24,
                    iconColor = ComposeAppTheme.colors.remus,
                    modifier = Modifier.weight(1f),
                    enabled = areBankActionsEnabled && !isDepositing,
                    onClick = {
                        val polygonUsdcToken =
                            App.marketKit.tokens(BlockchainType.Polygon, "USDC").firstOrNull()
                                ?: return@ActionItem
                        val activeWallet =
                            App.walletManager.activeWallets.find { it.token == polygonUsdcToken }

                        if (activeWallet != null) {
                            val sendTitle = Translator.getString(
                                R.string.Send_Title,
                                activeWallet.token.fullCoin.coin.code
                            )
                            isDepositing = true
                            viewModel.fetchDepositInfo { address, error ->
                                isDepositing = false
                                if (address != null) {
                                    navController.slideFromRight(
                                        R.id.sendXFragment,
                                        SendFragment.Input(
                                            wallet = activeWallet,
                                            title = sendTitle,
                                            prefilledAddressData = PrefilledData(address)
                                        )
                                    )
                                } else {
                                    HudHelper.showErrorMessage(view, error ?: "Could not fetch deposit address")
                                }
                            }
                        } else {
                            showEnableTokenDialog = true
                        }
                    }
                )
                ActionItem(
                    text = stringResource(R.string.Bank_Withdraw),
                    iconRes = R.drawable.ic_arrow_medium2_up_24,
                    iconColor = ComposeAppTheme.colors.jacob,
                    modifier = Modifier.weight(1f),
                    // enabled = areBankActionsEnabled,
                    enabled = false,
                    onClick = {
                        val polygonUsdcToken =
                            App.marketKit.tokens(BlockchainType.Polygon, "USDC").firstOrNull()
                                ?: return@ActionItem
                        val activeWallet =
                            App.walletManager.activeWallets.find { it.token == polygonUsdcToken }

                        if (activeWallet != null) {
                            showWithdrawDialog = true
                        } else {
                            showEnableTokenDialog = true
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Recent Transactions header
            Text(
                text = stringResource(R.string.Bank_RecentTransactions),
                style = ComposeAppTheme.typography.body,
                color = ComposeAppTheme.colors.leah,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Recent Transactions List
            val transactions = viewModel.transactions

            if (transactions.isEmpty() && uiState is ViewState.Success) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    subhead2_grey(text = "No transactions found")
                }
            } else {
                CellUniversalLawrenceSection(
                    items = transactions,
                    showFrame = true
                ) { transaction ->
                    val isIncoming = transaction.type.equals("Credit", ignoreCase = true)
                    val color =
                        if (isIncoming) ComposeAppTheme.colors.remus else ComposeAppTheme.colors.jacob
                    val iconRes =
                        if (isIncoming) R.drawable.ic_arrow_medium2_down_24 else R.drawable.ic_arrow_medium2_up_24

                    val date = try {
                        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
                        sdf.timeZone = TimeZone.getTimeZone("UTC")
                        sdf.parse(transaction.createdAt)
                    } catch (e: Exception) {
                        null
                    }

                    val formattedDate = date?.let { formatDate(it) } ?: transaction.createdAt

                    TransactionItem(
                        title = transaction.type + " " + (if (isIncoming) "from" else "to") + " " + transaction.transactionId.take(
                            8
                        ) + "...",
                        date = formattedDate,
                        amount = (if (isIncoming) "+" else "-") + transaction.currency + " " + String.format(
                            "%.2f",
                            transaction.amount
                        ),
                        isIncoming = isIncoming,
                        iconRes = iconRes,
                        color = color
                    )
                }
            }
        }
    }

    if (showEnableTokenDialog) {
        Dialog(onDismissRequest = { showEnableTokenDialog = false }) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(ComposeAppTheme.colors.lawrence)
            ) {
                BottomSheetsElementsHeader(
                    icon = painterResource(R.drawable.icon_24_warning_2),
                    title = stringResource(R.string.Alert_TitleWarning),
                    subtitle = stringResource(R.string.Bank_Title),
                    onClickClose = { showEnableTokenDialog = false }
                )
                BottomSheetsElementsText(
                    text = stringResource(R.string.Bank_Error_EnablePolygonUsdt)
                )
                BottomSheetsElementsButtons(
                    buttonPrimaryText = stringResource(id = R.string.Button_Ok),
                    onClickPrimary = { showEnableTokenDialog = false }
                )
            }
        }
    }

    if (showWithdrawDialog) {
        Dialog(onDismissRequest = { showWithdrawDialog = false }) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(ComposeAppTheme.colors.lawrence)
            ) {
                BottomSheetsElementsHeader(
                    icon = painterResource(R.drawable.ic_arrow_medium2_up_24),
                    title = "Withdraw",
                    subtitle = "Enter amount to withdraw to your wallet",
                    onClickClose = { showWithdrawDialog = false }
                )

                Column(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
                    FormsInput(
                        modifier = Modifier.fillMaxWidth(),
                        hint = "Amount",
                        enabled = !isWithdrawing,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        onValueChange = { withdrawAmount = it }
                    )
                }

                val polygonUsdtToken =
                    App.marketKit.tokens(BlockchainType.Polygon, "USDT").firstOrNull()
                val activeWallet =
                    App.walletManager.activeWallets.find { it.token == polygonUsdtToken }
                val walletAddress =
                    activeWallet?.let { App.adapterManager.getReceiveAdapterForWallet(it)?.receiveAddress }
                        ?: ""

                if (isWithdrawing) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        HSCircularProgressIndicator()
                    }
                } else {
                    BottomSheetsElementsButtons(
                        buttonPrimaryText = stringResource(id = R.string.Button_Confirm),
                        onClickPrimary = {
                            if (withdrawAmount.isNotEmpty() && walletAddress.isNotEmpty()) {
                                isWithdrawing = true
                                viewModel.withdraw(withdrawAmount, walletAddress) { error ->
                                    isWithdrawing = false
                                    if (error == null) {
                                        HudHelper.showSuccessMessage(view, "Withdrawal successful")
                                        showWithdrawDialog = false
                                        withdrawAmount = ""
                                    } else {
                                        HudHelper.showErrorMessage(view, error)
                                    }
                                }
                            } else if (walletAddress.isEmpty()) {
                                HudHelper.showErrorMessage(view, "Wallet address not found")
                            } else {
                                HudHelper.showErrorMessage(view, "Please enter amount")
                            }
                        }
                    )
                }
            }
        }
    }
}

data class TransactionViewItem(
    val title: String,
    val date: String,
    val amount: String,
    val isIncoming: Boolean,
    val iconRes: Int,
    val color: Color
)

@Composable
fun ActionItem(
    text: String,
    iconRes: Int,
    iconColor: Color = ComposeAppTheme.colors.jacob,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .height(100.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (enabled) ComposeAppTheme.colors.lawrence else ComposeAppTheme.colors.lawrence.copy(
                    alpha = 0.5f
                )
            )
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(if (enabled) 1f else 0.5f)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(iconColor.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = text,
                style = ComposeAppTheme.typography.subhead2,
                color = ComposeAppTheme.colors.grey
            )
        }
    }
}

@Composable
fun TransactionItem(
    title: String,
    date: String,
    amount: String,
    isIncoming: Boolean,
    iconRes: Int,
    color: Color
) {
    RowUniversal(
        modifier = Modifier.padding(horizontal = 16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.2f)), // Semi-transparent background
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = ComposeAppTheme.typography.body,
                color = ComposeAppTheme.colors.leah
            )
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = date,
                style = ComposeAppTheme.typography.subhead2,
                color = ComposeAppTheme.colors.grey
            )
        }
        Text(
            text = amount,
            style = ComposeAppTheme.typography.body,
            color = if (isIncoming) ComposeAppTheme.colors.remus else ComposeAppTheme.colors.jacob
        )
    }
}

@Composable
fun GenericActionCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    icon: Int,
    iconBackground: Color,
    buttonTitle: String? = null,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxWidth() // Width full
            .clickable { if (buttonTitle == null) onClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(iconBackground),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = icon),
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                style = ComposeAppTheme.typography.headline1,
                color = ComposeAppTheme.colors.leah,
                textAlign = TextAlign.Center // Text wrap hone par center rahe
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                style = ComposeAppTheme.typography.subhead2,
                color = ComposeAppTheme.colors.grey,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))

            buttonTitle?.let {
                ButtonPrimaryDefault(
                    modifier = Modifier
                        .padding(horizontal = 32.dp)
                        .fillMaxWidth(),
                    title = it,
                    onClick = onClick
                )
            }
        }
    }
}

private fun formatDate(date: Date): String {
    val calendar = Calendar.getInstance()
    calendar.time = date

    val today = Calendar.getInstance()
    if (calendar[Calendar.YEAR] == today[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == today[Calendar.DAY_OF_YEAR]) {
        return Translator.getString(R.string.Timestamp_Today)
    }

    val yesterday = Calendar.getInstance()
    yesterday.add(Calendar.DAY_OF_MONTH, -1)
    if (calendar[Calendar.YEAR] == yesterday[Calendar.YEAR] && calendar[Calendar.DAY_OF_YEAR] == yesterday[Calendar.DAY_OF_YEAR]) {
        return Translator.getString(R.string.Timestamp_Yesterday)
    }

    return DateHelper.shortDate(date, "MMMM d", "MMMM d, yyyy")
}
