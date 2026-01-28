package com.payfunds.wallet.modules.bank

import android.util.Log
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.*
import com.payfunds.wallet.core.authorizedAction
import io.payfunds.core.helpers.HudHelper
import androidx.compose.ui.platform.LocalView
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.lifecycle.viewmodel.compose.viewModel
import com.payfunds.wallet.entities.ViewState
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CardControlScreen(
    navController: NavController,
    viewModel: CardControlViewModel = viewModel(factory = CardControlModule.Factory())
) {
    val cardDetails = viewModel.cardDetails
    val uiState = viewModel.uiState

    var show3DSMethodDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showUpdatePinDialog by remember { mutableStateOf(false) }
    var pinValue by remember { mutableStateOf("") }
    var showUpdateLabelDialog by remember { mutableStateOf(false) }
    var labelValue by remember { mutableStateOf("") }
    val view = LocalView.current

    LaunchedEffect(uiState) {
        if (uiState is ViewState.Error) {
            HudHelper.showErrorMessage(view, uiState.t.message ?: "An error occurred")
        }
    }

    LifecycleEventEffect(event = Lifecycle.Event.ON_RESUME) {
        viewModel.fetchCardHtml()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .background(ComposeAppTheme.colors.tyler)
    ) {
        AppBar(
            title = stringResource(R.string.Bank_CardControl),
            menuItems = listOf(),
            navigationIcon = {
                HsBackButton(onClick = { navController.popBackStack() })
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 32.dp)
        ) {
            VSpacer(12.dp)

            if (viewModel.cardSensitiveDetails != null) {
                val card = viewModel.cardSensitiveDetails!!
                CreditCardView(
                    cardNumber = card.data.card.panText,
                    expiryDate = card.data.card.expiry,
                    cvv = card.data.card.cvvText
                )
            } else {
                Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(12.dp)), contentAlignment = Alignment.Center) {
                    HSCircularProgressIndicator()
                }
            }

            VSpacer(24.dp)

            // Update PIN
            CardControlOption(
                icon = R.drawable.ic_edit_24,
                text = "Update PIN",
                onClick = {
                    navController.authorizedAction {
                        showUpdatePinDialog = true
                    }
                }
            )

            VSpacer(24.dp)

            // Freeze Card
            CardControlOption(
                icon = R.drawable.ic_blocks_24,
                text = "Freeze Card",
                onClick = {
                    navController.authorizedAction {
                        viewModel.toggleFreeze(!viewModel.isFrozen)
                    }
                },
                trailing = {
                    HsSwitch(
                        checked = viewModel.isFrozen,
                        onCheckedChange = {
                            navController.authorizedAction {
                                viewModel.toggleFreeze(it)
                            }
                        }
                    )
                }
            )

            VSpacer(24.dp)

            // 3DS Forwarding Method
            CardControlOption(
                icon = R.drawable.ic_refresh,
                text = "3DS Forwarding Method",
                onClick = {
                    navController.authorizedAction {
                        show3DSMethodDialog = true
                    }
                },
                trailing = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        subhead1_grey(text = viewModel.selected3DSMethod)
                        HSpacer(4.dp)
                        Icon(
                            painter = painterResource(id = R.drawable.ic_arrow_swap3_20),
                            contentDescription = null,
                            tint = ComposeAppTheme.colors.grey,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            )

            VSpacer(24.dp)

            CardControlOption(
                icon = R.drawable.ic_bank_24,
                text = "Update Card Label",
                onClick = {
                    navController.authorizedAction {
                        showUpdateLabelDialog = true
                    }
                }
            )

            VSpacer(24.dp)

            CardControlOption(
                icon = R.drawable.ic_delete_20,
                text = "Delete Card",
                onClick = {
                    navController.authorizedAction {
                        showDeleteConfirmation = true
                    }
                }
            )

        }
    }

    if (show3DSMethodDialog) {
        Dialog(onDismissRequest = { show3DSMethodDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = ComposeAppTheme.colors.lawrence
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        subhead1_leah(text = "3DS Forwarding Method")
                        HsIconButton(onClick = { show3DSMethodDialog = false }) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_close),
                                contentDescription = "close",
                                tint = ComposeAppTheme.colors.grey,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }

                    val methods = listOf("IN_APP", "EMAIL", "SMS")
                    VSpacer(16.dp)
                    methods.forEach { method ->
                        RowUniversal(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (viewModel.selected3DSMethod == method) ComposeAppTheme.colors.steel20 else Color.Transparent)
                                .padding(horizontal = 12.dp),
                            onClick = {
                                viewModel.update3dsForwarding(method, "Enabled")
                                show3DSMethodDialog = false
                            }
                        ) {
                            body_leah(text = method, modifier = Modifier.weight(1f))
                            if (viewModel.selected3DSMethod == method) {
                                Icon(
                                    painter = painterResource(id = R.drawable.icon_20_check_1),
                                    contentDescription = null,
                                    tint = ComposeAppTheme.colors.lucian,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showUpdatePinDialog) {
        Dialog(onDismissRequest = { showUpdatePinDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = ComposeAppTheme.colors.lawrence
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    title3_leah(text = "Update PIN")
                    VSpacer(16.dp)
                    FormsInput(
                        hint = "Enter New PIN",
                        onValueChange = { pinValue = it },
                        state = null
                    )
                    VSpacer(24.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ButtonSecondaryDefault(
                            modifier = Modifier.weight(1f),
                            title = "Cancel",
                            onClick = { showUpdatePinDialog = false }
                        )
                        ButtonPrimaryRed(
                            modifier = Modifier.weight(1f),
                            title = "Update",
                            loadingIndicator = uiState is ViewState.Loading,
                            onClick = {
                                viewModel.updatePin(pinValue) { message ->
                                    showUpdatePinDialog = false
                                    pinValue = ""
                                    HudHelper.showSuccessMessage(view, message)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
    if (showDeleteConfirmation) {
        Dialog(onDismissRequest = { showDeleteConfirmation = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = ComposeAppTheme.colors.lawrence
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_wallet_48),
                        contentDescription = null,
                        tint = ComposeAppTheme.colors.jacob,
                        modifier = Modifier.size(48.dp)
                    )
                    VSpacer(16.dp)
                    title3_leah(text = "Delete Card")
                    VSpacer(12.dp)
                    subhead2_grey(
                        text = "Your card will be deleted permanently. This action cannot be undone.",
                        textAlign = TextAlign.Center
                    )
                    VSpacer(24.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ButtonSecondaryDefault(
                            modifier = Modifier.weight(1f),
                            title = "Cancel",
                            onClick = { showDeleteConfirmation = false }
                        )
                        ButtonPrimaryRed(
                            modifier = Modifier.weight(1f),
                            title = "Delete",
                            enabled = uiState !is ViewState.Loading,
                            loadingIndicator = uiState is ViewState.Loading,
                            onClick = {
                                viewModel.deleteCard {
                                    showDeleteConfirmation = false
                                    navController.previousBackStackEntry?.savedStateHandle?.set(
                                        "refreshBankDetails",
                                        true
                                    )
                                    navController.popBackStack()
                                }
                            }
                        )
                    }
                }
            }
        }
    }

    if (showUpdateLabelDialog) {
        Dialog(onDismissRequest = { showUpdateLabelDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp),
                backgroundColor = ComposeAppTheme.colors.lawrence
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    title3_leah(text = "Update Card Label")
                    VSpacer(16.dp)
                    FormsInput(
                        hint = "Enter New Label",
                        onValueChange = { labelValue = it },
                        state = null
                    )
                    VSpacer(24.dp)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        ButtonSecondaryDefault(
                            modifier = Modifier.weight(1f),
                            title = "Cancel",
                            onClick = { showUpdateLabelDialog = false }
                        )
                        ButtonPrimaryRed(
                            modifier = Modifier.weight(1f),
                            title = "Update",
                            loadingIndicator = uiState is ViewState.Loading,
                            onClick = {
                                if (labelValue.isNotEmpty()) {
                                    viewModel.updateCardLabel(labelValue) { message ->
                                        showUpdateLabelDialog = false
                                        labelValue = ""
                                        HudHelper.showSuccessMessage(view, message)
                                        viewModel.fetchCardDetails() // Refresh details to show new label
                                    }
                                } else {
                                    HudHelper.showErrorMessage(view, "Please enter a label")
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CreditCardItem(
    cardNumber: String,
    cardHolder: String,
    expiryDate: String,
    balance: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(280.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFF1E4AB0)) // Based on image color
            .padding(24.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_wifi), // Placeholder for wifi icon
                    contentDescription = null,
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = cardNumber,
                style = ComposeAppTheme.typography.title3,
                color = Color.White,
                fontSize = 22.sp,
                letterSpacing = 2.sp
            )

            VSpacer(16.dp)

            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "CARD HOLDER",
                        style = ComposeAppTheme.typography.caption,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = cardHolder,
                        style = ComposeAppTheme.typography.body,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "EXPIRES",
                        style = ComposeAppTheme.typography.caption,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                    Text(
                        text = expiryDate,
                        style = ComposeAppTheme.typography.body,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            VSpacer(20.dp)

            Text(
                text = balance,
                style = ComposeAppTheme.typography.headline1,
                color = Color.White,
                fontSize = 28.sp
            )
        }
    }
}

@Composable
fun CardControlOption(
    icon: Int,
    text: String,
    onClick: () -> Unit,
    trailing: (@Composable RowScope.() -> Unit)? = null
) {
    CellUniversalLawrenceSection {
        RowUniversal(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(vertical = 8.dp),
            onClick = onClick
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                tint = ComposeAppTheme.colors.lucian,
                modifier = Modifier.size(20.dp)
            )
            HSpacer(16.dp)
            body_leah(text = text, modifier = Modifier.weight(1f))
            trailing?.invoke(this) ?: Icon(
                painter = painterResource(id = R.drawable.ic_arrow_right_20),
                contentDescription = null,
                tint = ComposeAppTheme.colors.grey
            )
        }
    }
}
