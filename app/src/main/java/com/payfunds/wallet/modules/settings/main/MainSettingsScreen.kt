package com.payfunds.wallet.modules.settings.main

import android.content.Context
import android.content.Intent
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.DashboardObject
import com.payfunds.wallet.core.managers.RateAppManager
import com.payfunds.wallet.core.providers.Translator
import com.payfunds.wallet.core.slideFromBottom
import com.payfunds.wallet.core.slideFromRight
import com.payfunds.wallet.core.stats.StatEvent
import com.payfunds.wallet.core.stats.StatPage
import com.payfunds.wallet.core.stats.stat
import com.payfunds.wallet.modules.alert.AlertFragment
import com.payfunds.wallet.modules.contacts.ContactsFragment
import com.payfunds.wallet.modules.contacts.Mode
import com.payfunds.wallet.modules.manageaccount.dialogs.BackupRequiredDialog
import com.payfunds.wallet.modules.manageaccounts.ManageAccountsModule
import com.payfunds.wallet.modules.settings.appearance.AppearanceModule
import com.payfunds.wallet.modules.settings.appearance.AppearanceViewModel
import com.payfunds.wallet.modules.walletconnect.WCAccountTypeNotSupportedDialog
import com.payfunds.wallet.modules.walletconnect.WCManager
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.AlertGroup
import com.payfunds.wallet.ui.compose.components.AppBar
import com.payfunds.wallet.ui.compose.components.BadgeText
import com.payfunds.wallet.ui.compose.components.CellUniversalLawrenceSection
import com.payfunds.wallet.ui.compose.components.RowUniversal
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.compose.components.body_leah
import com.payfunds.wallet.ui.compose.components.subhead1_grey
import zendesk.support.guide.HelpCenterActivity

@Composable
fun SettingsScreen(navController: NavController,
                   viewModel: MainSettingsViewModel = viewModel(factory = MainSettingsModule.Factory()), ) {

    Surface(color = ComposeAppTheme.colors.tyler) {
        Column {
            AppBar(
                stringResource(R.string.Settings_Title),
            )
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(modifier = Modifier.height(12.dp))
                SettingSections(viewModel, navController)
            }
        }
    }
}

@Composable
private fun SettingSections(
    viewModel: MainSettingsViewModel,
    navController: NavController
) {
    val uiState = viewModel.uiState
    val context = LocalContext.current

    val viewModelTheme = viewModel<AppearanceViewModel>(factory = AppearanceModule.Factory())
    val uiStateTheme = viewModelTheme.uiState
    var openThemeSelector by rememberSaveable { mutableStateOf(false) }

    val isMultiFactor by DashboardObject.isMultiFactor.collectAsState()

    if (isMultiFactor != true) {
        CellUniversalLawrenceSection(
            listOf(
                {
                    HsSettingCell(
                        R.string.SettingsSecurity_ManageKeys,
                        R.drawable.ic_wallet_20,
                        showAlert = uiState.manageWalletShowAlert,
                        onClick = {
                            navController.slideFromRight(
                                R.id.manageAccountsFragment,
                                ManageAccountsModule.Mode.Manage
                            )

                            stat(
                                page = StatPage.Settings,
                                event = StatEvent.Open(StatPage.ManageWallets)
                            )
                        }
                    )
                },
                {
                    HsSettingCell(
                        R.string.BlockchainSettings_Title,
                        R.drawable.ic_blocks_20,
                        onClick = {
                            navController.slideFromRight(R.id.blockchainSettingsFragment)

                            stat(
                                page = StatPage.Settings,
                                event = StatEvent.Open(StatPage.BlockchainSettings)
                            )
                        }
                    )
                },
                {
                    HsSettingCell(
                        R.string.Settings_WalletConnect,
                        R.drawable.ic_wallet_connect_20,
                        value = (uiState.wcCounterType as? MainSettingsModule.CounterType.SessionCounter)?.number?.toString(),
                        counterBadge = (uiState.wcCounterType as? MainSettingsModule.CounterType.PendingRequestCounter)?.number?.toString(),
                        onClick = {
                            when (val state = viewModel.walletConnectSupportState) {
                                WCManager.SupportState.Supported -> {
                                    navController.slideFromRight(R.id.wcListFragment)

                                    stat(
                                        page = StatPage.Settings,
                                        event = StatEvent.Open(StatPage.WalletConnect)
                                    )
                                }

                                WCManager.SupportState.NotSupportedDueToNoActiveAccount -> {
                                    navController.slideFromBottom(R.id.wcErrorNoAccountFragment)
                                }

                                is WCManager.SupportState.NotSupportedDueToNonBackedUpAccount -> {
                                    val text =
                                        Translator.getString(R.string.WalletConnect_Error_NeedBackup)
                                    navController.slideFromBottom(
                                        R.id.backupRequiredDialog,
                                        BackupRequiredDialog.Input(state.account, text)
                                    )

                                    stat(
                                        page = StatPage.Settings,
                                        event = StatEvent.Open(StatPage.BackupRequired)
                                    )
                                }

                                is WCManager.SupportState.NotSupported -> {
                                    navController.slideFromBottom(
                                        R.id.wcAccountTypeNotSupportedDialog,
                                        WCAccountTypeNotSupportedDialog.Input(state.accountTypeDescription)
                                    )
                                }
                            }
                        }
                    )
                },
                {
                    HsSettingCell(
                        R.string.BackupManager_Title,
                        R.drawable.ic_file_24,
                        onClick = {
                            navController.slideFromRight(R.id.backupManagerFragment)
                            stat(
                                page = StatPage.Settings,
                                event = StatEvent.Open(StatPage.BackupManager)
                            )
                        }
                    )
                }
            )
        )
    } else {
        CellUniversalLawrenceSection(
            listOf(
                {
                    HsSettingCell(
                        R.string.SettingsSecurity_ManageKeys,
                        R.drawable.ic_wallet_20,
                        showAlert = uiState.manageWalletShowAlert,
                        onClick = {
                            navController.slideFromRight(
                                R.id.manageAccountsFragment,
                                ManageAccountsModule.Mode.Manage
                            )

                            stat(
                                page = StatPage.Settings,
                                event = StatEvent.Open(StatPage.ManageWallets)
                            )
                        }
                    )
                },
            )
        )
    }

    VSpacer(32.dp)

    if (isMultiFactor != true) {
        CellUniversalLawrenceSection(
            listOf(
                {
                    HsSettingCell(
                        R.string.Settings_SecurityCenter,
                        R.drawable.ic_security,
                        showAlert = uiState.securityCenterShowAlert,
                        onClick = {
                            navController.slideFromRight(R.id.securitySettingsFragment)

                            stat(
                                page = StatPage.Settings,
                                event = StatEvent.Open(StatPage.Security)
                            )
                        }
                    )
                },
                {
                    HsSettingCell(
                        R.string.Contacts,
                        R.drawable.ic_user_20,
                        onClick = {
                            navController.slideFromRight(
                                R.id.contactsFragment,
                                ContactsFragment.Input(Mode.Full)
                            )

                            stat(
                                page = StatPage.Settings,
                                event = StatEvent.Open(StatPage.Contacts)
                            )
                        }
                    )
                },
                {
                    HsSettingCell(
                        R.string.Settings_Appearance,
                        R.drawable.ic_brush_20,
                        onClick = {
                            openThemeSelector = true
                        }
                    )
                },
                {
                    HsSettingCell(
                        R.string.Settings_Alert,
                        R.drawable.ic_alert_settings,
                        onClick = {
                            navController.slideFromRight(
                                R.id.alertFragment,
                                AlertFragment.Input("alertAllScreen")
                            )
                        }
                    )
                },

//                {
//                    HsSettingCell(
//                        R.string.Settings_Faq,
//                        R.drawable.ic_faq_20,
//                        onClick = {
//                        navController.slideFromRight(R.id.faqListFragment)
//                        stat(page = StatPage.Settings, event = StatEvent.Open(StatPage.Faq))
//                        }
//                    )
//                }
            )
        )
    } else {

        CellUniversalLawrenceSection(
            listOf(
            /*    {
                    HsSettingCell(
                        R.string.Contacts,
                        R.drawable.ic_user_20,
                        onClick = {
                            navController.slideFromRight(
                                R.id.contactsFragment,
                                ContactsFragment.Input(Mode.Full)
                            )

                            stat(
                                page = StatPage.Settings,
                                event = StatEvent.Open(StatPage.Contacts)
                            )
                        }
                    )
                },*/
                {
                    HsSettingCell(
                        R.string.Settings_Appearance,
                        R.drawable.ic_brush_20,
                        onClick = {
                            openThemeSelector = true
                        }
                    )
                },


                {
                    HsSettingCell(
                        R.string.Settings_Faq,
                        R.drawable.ic_faq_20,
                        onClick = {
//                        navController.slideFromRight(R.id.faqListFragment)
//                        stat(page = StatPage.Settings, event = StatEvent.Open(StatPage.Faq))
                        }
                    )
                }


            )
        )

    }


    VSpacer(32.dp)

    CellUniversalLawrenceSection(
        listOf({
            HsSettingCell(
                R.string.SettingsAboutApp_Title,
                R.drawable.ic_about_app_20,
                showAlert = uiState.aboutAppShowAlert,
                onClick = {
                    navController.slideFromRight(R.id.aboutAppFragment)

                    stat(page = StatPage.Settings, event = StatEvent.Open(StatPage.AboutApp))
                }
            )
        },
            {
                HsSettingCell(
                    R.string.Settings_RateUs,
                    R.drawable.ic_star_20,
                    onClick = {
                        RateAppManager.openPlayMarket(context)

                        stat(page = StatPage.Settings, event = StatEvent.Open(StatPage.RateUs))
                    }
                )
            },
            {
                HsSettingCell(
                    R.string.SettingsContact_Title_Help,
                    R.drawable.ic_mail_24,
                    onClick = {
                        HelpCenterActivity.builder()
                            .show(context)
//                    navController.slideFromBottom(R.id.contactOptionsDialog)
                        stat(page = StatPage.Settings, event = StatEvent.Open(StatPage.ContactUs))
                    },
                )
            })
    )

    VSpacer(32.dp)

    if (openThemeSelector) {
        AlertGroup(
            R.string.Settings_Theme,
            uiStateTheme.themeOptions,
            { selected ->
                viewModelTheme.onEnterTheme(selected)
                openThemeSelector = false
            },
            { openThemeSelector = false }
        )
    }
}

@Composable
fun HsSettingCell(
    @StringRes title: Int,
    @DrawableRes icon: Int,
    iconTint: Color? = null,
    value: String? = null,
    counterBadge: String? = null,
    showAlert: Boolean = false,
    onClick: () -> Unit,
    disableRightArrow: Boolean = false,
) {
    RowUniversal(
        modifier = Modifier.padding(horizontal = 16.dp),
        onClick = if (disableRightArrow) null else onClick
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            painter = painterResource(id = icon),
            contentDescription = null,
            tint = iconTint ?: ComposeAppTheme.colors.grey
        )
        body_leah(
            text = stringResource(title),
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Spacer(Modifier.weight(1f))

        if (counterBadge != null) {
            BadgeText(
                modifier = Modifier.padding(horizontal = 8.dp),
                text = counterBadge
            )
        } else if (value != null) {
            subhead1_grey(
                text = value,
                maxLines = 1,
                modifier = Modifier.padding(horizontal = 8.dp)
            )
        }

        if (showAlert) {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = R.drawable.ic_attention_red_20),
                contentDescription = null,
            )
            Spacer(Modifier.width(12.dp))
        }
        if (!disableRightArrow) {
            Image(
                modifier = Modifier.size(20.dp),
                painter = painterResource(id = R.drawable.ic_arrow_right),
                contentDescription = null,
            )
        }
    }
}


@Preview
@Composable
private fun PreviewSettingsScreen() {
    ComposeAppTheme {
        SettingSections(
            viewModel(),
            navController = NavController(LocalContext.current)
        )
    }
}

private fun shareAppLink(appLink: String, context: Context) {
    val shareMessage = Translator.getString(R.string.SettingsShare_Text) + "\n" + appLink + "\n"
    val shareIntent = Intent(Intent.ACTION_SEND)
    shareIntent.type = "text/plain"
    shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage)
    context.startActivity(
        Intent.createChooser(
            shareIntent,
            Translator.getString(R.string.SettingsShare_Title)
        )
    )
}