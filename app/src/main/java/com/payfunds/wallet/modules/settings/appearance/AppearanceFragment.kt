package com.payfunds.wallet.modules.settings.appearance

import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Scaffold
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.AlertGroup
import com.payfunds.wallet.ui.compose.components.AppBar
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRed
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryTransparent
import com.payfunds.wallet.ui.compose.components.CellUniversalLawrenceSection
import com.payfunds.wallet.ui.compose.components.HsBackButton
import com.payfunds.wallet.ui.compose.components.RowUniversal
import com.payfunds.wallet.ui.compose.components.TextImportantWarning
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.compose.components.body_leah
import com.payfunds.wallet.ui.compose.components.subhead1_grey
import com.payfunds.wallet.ui.extensions.BottomSheetHeader
import kotlinx.coroutines.launch

class AppearanceFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        AppearanceScreen(navController)
    }

}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun AppearanceScreen(navController: NavController) {
    val viewModel = viewModel<AppearanceViewModel>(factory = AppearanceModule.Factory())
    val uiState = viewModel.uiState

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState(
        ModalBottomSheetValue.Hidden
    )

    var openThemeSelector by rememberSaveable { mutableStateOf(false) }
    var openBalanceValueSelector by rememberSaveable { mutableStateOf(false) }
    var openPriceChangeIntervalSelector by rememberSaveable { mutableStateOf(false) }

    ModalBottomSheetLayout(
        sheetState = sheetState,
        sheetBackgroundColor = ComposeAppTheme.colors.transparent,
        sheetContent = {
            AppCloseWarningBottomSheet(
                onCloseClick = { scope.launch { sheetState.hide() } },
                onChangeClick = {
                     scope.launch { sheetState.hide() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier.statusBarsPadding(),
            backgroundColor = ComposeAppTheme.colors.tyler,
            topBar = {
                AppBar(
                    title = stringResource(R.string.Settings_Appearance),
                    navigationIcon = {
                        HsBackButton(onClick = { navController.popBackStack() })
                    },
                    menuItems = listOf(),
                )
            }
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(paddingValues),
            ) {
                VSpacer(height = 12.dp)
                CellUniversalLawrenceSection(
                    listOf {
                        MenuItemWithDialog(
                            R.string.Settings_Theme,
                            value = uiState.selectedTheme.title.getString(),
                            onClick = { openThemeSelector = true }
                        )
                    }
                )


                VSpacer(32.dp)
            }
        }
        //Dialogs
        if (openThemeSelector) {
            AlertGroup(
                R.string.Settings_Theme,
                uiState.themeOptions,
                { selected ->
                    viewModel.onEnterTheme(selected)
                    openThemeSelector = false
                },
                { openThemeSelector = false }
            )
        }

        if (openBalanceValueSelector) {
            AlertGroup(
                R.string.Appearance_BalanceValue,
                uiState.balanceViewTypeOptions,
                { selected ->
                    viewModel.onEnterBalanceViewType(selected)
                    openBalanceValueSelector = false
                },
                { openBalanceValueSelector = false }
            )
        }
        if (openPriceChangeIntervalSelector) {
            AlertGroup(
                R.string.Appearance_PriceChangeInterval,
                uiState.priceChangeIntervalOptions,
                { selected ->
                    viewModel.onSetPriceChangeInterval(selected)
                    openPriceChangeIntervalSelector = false
                },
                { openPriceChangeIntervalSelector = false }
            )
        }
    }
}

@Composable
private fun AppCloseWarningBottomSheet(
    onCloseClick: () -> Unit,
    onChangeClick: () -> Unit
) {
    BottomSheetHeader(
        iconPainter = painterResource(id = R.drawable.ic_attention_24),
        title = stringResource(id = R.string.Alert_TitleWarning),
        iconTint = ColorFilter.tint(ComposeAppTheme.colors.jacob),
        onCloseClick = onCloseClick
    ) {
        TextImportantWarning(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            text = stringResource(R.string.Appearance_Warning_CloseApplication)
        )

        ButtonPrimaryRed(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 20.dp),
            title = stringResource(id = R.string.Button_Change),
            onClick = onChangeClick
        )

        ButtonPrimaryTransparent(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            title = stringResource(id = R.string.Button_Cancel),
            onClick = onCloseClick
        )
        VSpacer(20.dp)
    }
}



@Composable
fun MenuItemWithDialog(
    @StringRes title: Int,
    value: String,
    onClick: () -> Unit
) {
    RowUniversal(
        modifier = Modifier.padding(horizontal = 16.dp),
        onClick = onClick
    ) {
        body_leah(
            text = stringResource(title),
            maxLines = 1,
            modifier = Modifier.weight(1f)
        )

        subhead1_grey(
            text = value,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Image(
            modifier = Modifier.size(20.dp),
            painter = painterResource(id = R.drawable.ic_down_arrow_20),
            contentDescription = null,
        )
    }
}

@Preview
@Composable
fun MenuItemPreview() {
    ComposeAppTheme {
        AppearanceScreen(
            navController = rememberNavController()
        )
    }
}