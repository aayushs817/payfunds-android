package com.payfunds.wallet.modules.backupalert

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.ButtonColors
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.payfunds.wallet.R
import com.payfunds.wallet.core.requireInput
import com.payfunds.wallet.core.slideFromBottom
import com.payfunds.wallet.core.stats.StatEvent
import com.payfunds.wallet.core.stats.StatPage
import com.payfunds.wallet.core.stats.stat
import com.payfunds.wallet.entities.Account
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.ButtonPrimary
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryDefaultWithIcon
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryRedWithIcon
import com.payfunds.wallet.ui.compose.components.ButtonPrimaryTransparent
import com.payfunds.wallet.ui.compose.components.HSpacer
import com.payfunds.wallet.ui.compose.components.TextImportantWarning
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.extensions.BaseComposableBottomSheetFragment
import com.payfunds.wallet.ui.extensions.BottomSheetHeader
import io.payfunds.core.findNavController

class BackupRecoveryPhraseDialog : BaseComposableBottomSheetFragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return ComposeView(requireContext()).apply {
            setViewCompositionStrategy(
                ViewCompositionStrategy.DisposeOnLifecycleDestroyed(viewLifecycleOwner)
            )
            setContent {
                val navController = findNavController()
                BackupRecoveryPhraseScreen(navController, navController.requireInput())
            }
        }
    }
}

@Composable
fun BackupRecoveryPhraseScreen(navController: NavController, account: Account) {
    ComposeAppTheme {
        BottomSheetHeader(
            iconPainter = painterResource(R.drawable.ic_attention_24),
            iconTint = ColorFilter.tint(ComposeAppTheme.colors.jacob),
            title = stringResource(R.string.BackupRecoveryPhrase_Title),
            onCloseClick = {
                navController.popBackStack()
            }
        ) {
            VSpacer(12.dp)
            TextImportantWarning(
                modifier = Modifier.padding(horizontal = 16.dp),
                text = stringResource(R.string.BackupRecoveryPhrase_Description)
            )

            VSpacer(32.dp)
            ButtonPrimaryRedWithIcon(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                title = stringResource(R.string.BackupRecoveryPhrase_ManualBackup),
                icon = R.drawable.ic_edit_24,
                iconTint = ComposeAppTheme.colors.light,
                onClick = {
                    navController.slideFromBottom(R.id.backupKeyFragment, account)

                    stat(
                        page = StatPage.BackupPromptAfterCreate,
                        event = StatEvent.Open(StatPage.ManualBackup)
                    )
                }
            )
            VSpacer(12.dp)
            ButtonPrimaryDefaultWithIcon(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                title = stringResource(R.string.BackupRecoveryPhrase_LocalBackup),
                icon = R.drawable.ic_file_24,
                iconTint = ComposeAppTheme.colors.claude,
                onClick = {
                    navController.slideFromBottom(R.id.backupLocalFragment, account)

                    stat(
                        page = StatPage.BackupPromptAfterCreate,
                        event = StatEvent.Open(StatPage.FileBackup)
                    )
                }
            )
            VSpacer(12.dp)
            ButtonPrimaryTransparent(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                title = stringResource(R.string.BackupRecoveryPhrase_Later),
                onClick = {
                    navController.popBackStack()
                }
            )
            VSpacer(32.dp)
        }
    }
}

@Composable
private fun PrimaryButtonWithIcon(
    modifier: Modifier,
    title: String,
    icon: Int,
    iconTint: Color,
    buttonColors: ButtonColors,
    onClick: () -> Unit,
) {
    ButtonPrimary(
        modifier = modifier,
        onClick = onClick,
        buttonColors = buttonColors,
        content = {
            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(icon),
                    tint = iconTint,
                    contentDescription = null
                )
                HSpacer(8.dp)
                Text(title, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
        },
    )
}
