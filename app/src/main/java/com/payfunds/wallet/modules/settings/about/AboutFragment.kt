package com.payfunds.wallet.modules.settings.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.BaseComposeFragment
import com.payfunds.wallet.core.composablePage
import com.payfunds.wallet.core.composablePopup
import com.payfunds.wallet.core.slideFromBottom
import com.payfunds.wallet.core.stats.StatEvent
import com.payfunds.wallet.core.stats.StatPage
import com.payfunds.wallet.core.stats.stat
import com.payfunds.wallet.modules.releasenotes.ReleaseNotesScreen
import com.payfunds.wallet.modules.settings.appstatus.AppStatusScreen
import com.payfunds.wallet.modules.settings.main.HsSettingCell
import com.payfunds.wallet.modules.settings.privacy.PrivacyScreen
import com.payfunds.wallet.modules.settings.terms.TermsScreen
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.AppBar
import com.payfunds.wallet.ui.compose.components.CellUniversalLawrenceSection
import com.payfunds.wallet.ui.compose.components.HsBackButton
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.helpers.LinkHelper

class AboutFragment : BaseComposeFragment() {

    @Composable
    override fun GetContent(navController: NavController) {
        AboutNavHost(navController)
    }

}

private const val AboutPage = "about"
private const val ReleaseNotesPage = "release_notes"
private const val AppStatusPage = "app_status"
private const val PrivacyPage = "privacy"
private const val TermsPage = "terms"

@Composable
private fun AboutNavHost(fragmentNavController: NavController) {
    val navController = rememberNavController()
    NavHost(
        navController = navController,
        startDestination = AboutPage,
    ) {
        composable(AboutPage) {
            AboutScreen(
                navController,
                { fragmentNavController.slideFromBottom(R.id.contactOptionsDialog) },
                { fragmentNavController.popBackStack() }
            )
        }
        composablePage(ReleaseNotesPage) {
            ReleaseNotesScreen(false, { navController.popBackStack() })
        }
        composablePage(AppStatusPage) { AppStatusScreen(navController) }
        composablePage(PrivacyPage) { PrivacyScreen(navController) }
        composablePopup(TermsPage) { TermsScreen(navController) }
    }
}

@Composable
private fun AboutScreen(
    navController: NavController,
    showContactOptions: () -> Unit,
    onBackPress: () -> Unit,
    aboutViewModel: AboutViewModel = viewModel(factory = AboutModule.Factory()),
) {
    Surface(color = ComposeAppTheme.colors.tyler) {
        Column(
            modifier = Modifier.statusBarsPadding()
        ) {
            AppBar(
                title = stringResource(R.string.SettingsAboutApp_Title),
                navigationIcon = {
                    HsBackButton(onClick = onBackPress)
                }
            )

            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                Spacer(Modifier.height(12.dp))
                SettingSections(aboutViewModel, navController)
                Spacer(Modifier.height(36.dp))
            }
        }
    }
}

@Composable
private fun SettingSections(
    viewModel: AboutViewModel,
    navController: NavController
) {

    val context = LocalContext.current
    val termsShowAlert = viewModel.termsShowAlert

    CellUniversalLawrenceSection(
        listOf {
            HsSettingCell(
                title = R.string.SettingsAboutApp_AppVersion,
                icon = R.drawable.ic_info_20,
                value = viewModel.appVersion,
                onClick = {

                },
                disableRightArrow = true
            )
        }
    )

    Spacer(Modifier.height(32.dp))

    CellUniversalLawrenceSection(
        listOf({
            HsSettingCell(
                R.string.Settings_Terms,
                R.drawable.ic_terms_20,
                showAlert = termsShowAlert,
                onClick = {
                    navController.navigate(TermsPage)
                    stat(page = StatPage.AboutApp, event = StatEvent.Open(StatPage.Terms))
                }
            )
        }, {
            HsSettingCell(
                R.string.Settings_Privacy,
                R.drawable.ic_user_20,
                onClick = {
                    // navController.navigate(PrivacyPage)
                    LinkHelper.openLinkInAppBrowser(context, viewModel.privacyPolicyLink)
                    stat(page = StatPage.AboutApp, event = StatEvent.Open(StatPage.Privacy))
                }
            )
        })
    )

    VSpacer(32.dp)

    CellUniversalLawrenceSection(
        listOf {
            HsSettingCell(
                title = R.string.SettingsAboutApp_Website,
                icon = R.drawable.ic_explorer,
                onClick = {
                    LinkHelper.openLinkInAppBrowser(context, viewModel.appWebPageLink)

                    stat(page = StatPage.AboutApp, event = StatEvent.Open(StatPage.ExternalWebsite))
                }
            )
        }
    )
    VSpacer(8.dp)
    CellUniversalLawrenceSection(
        listOf {
            HsSettingCell(
                title = R.string.SettingsAboutApp_Youtube,
                icon = R.drawable.ic_about_youtube,

                onClick = {
                    LinkHelper.openLinkInAppBrowser(context, viewModel.appYoutubeLink)

                    stat(page = StatPage.AboutApp, event = StatEvent.Open(StatPage.ExternalWebsite))
                }
            )
        }
    )
   VSpacer(8.dp)
    CellUniversalLawrenceSection(
        listOf {
            HsSettingCell(
                title = R.string.SettingsAboutApp_Instagram,
                icon = R.drawable.ic_about_instagram,

                onClick = {
                    LinkHelper.openLinkInAppBrowser(context, viewModel.appInstagramLink)

                    stat(page = StatPage.AboutApp, event = StatEvent.Open(StatPage.ExternalWebsite))

                }
            )
        }
    )
   VSpacer(8.dp)
    CellUniversalLawrenceSection(
        listOf {
            HsSettingCell(
                title = R.string.SettingsAboutApp_X,
                icon = R.drawable.ic_about_x,
                onClick = {
                    LinkHelper.openLinkInAppBrowser(context, viewModel.appXLink)

                    stat(page = StatPage.AboutApp, event = StatEvent.Open(StatPage.ExternalWebsite))

                }
            )
        }
    )
   VSpacer(8.dp)
    CellUniversalLawrenceSection(
        listOf {
            HsSettingCell(
                title = R.string.SettingsAboutApp_Telegram,
                icon = R.drawable.ic_about_telegram,

                onClick = {
                    LinkHelper.openLinkInAppBrowser(context, viewModel.appTelegramLink)

                    stat(page = StatPage.AboutApp, event = StatEvent.Open(StatPage.ExternalWebsite))

                }
            )
        }
    )

   VSpacer(8.dp)
    CellUniversalLawrenceSection(
        listOf {
            HsSettingCell(
                title = R.string.SettingsAboutApp_Facebook,
                icon = R.drawable.ic_about_facebook,

                onClick = {
                    LinkHelper.openLinkInAppBrowser(context, viewModel.appFacebookLink)

                    stat(page = StatPage.AboutApp, event = StatEvent.Open(StatPage.ExternalWebsite))

                }
            )
        }
    )

   VSpacer(8.dp)
    CellUniversalLawrenceSection(
        listOf {
            HsSettingCell(
                title = R.string.SettingsAboutApp_Reddit,
                icon = R.drawable.ic_about_reddit,

                onClick = {
                    LinkHelper.openLinkInAppBrowser(context, viewModel.appRedditLink)

                    stat(page = StatPage.AboutApp, event = StatEvent.Open(StatPage.ExternalWebsite))
                }
            )
        }
    )

    VSpacer(32.dp)
}
