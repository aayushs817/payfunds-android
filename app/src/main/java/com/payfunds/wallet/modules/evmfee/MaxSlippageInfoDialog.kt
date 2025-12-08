package com.payfunds.wallet.modules.evmfee


import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.payfunds.wallet.R
import com.payfunds.wallet.core.requireInput
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.Yellow50
import com.payfunds.wallet.ui.compose.Yellow80
import com.payfunds.wallet.ui.compose.components.HSpacer
import com.payfunds.wallet.ui.extensions.BaseComposableBottomSheetFragment
import com.payfunds.wallet.ui.extensions.BottomSheetHeader
import io.payfunds.core.findNavController
import kotlinx.parcelize.Parcelize

class MaxSlippageInfoDialog : BaseComposableBottomSheetFragment() {

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
                val input = navController.requireInput<Input>()

                ComposeAppTheme {
                    MaxSlippageInfoScreen(input.title, input.text, input.receiveAtLeast) { dismiss() }
                }
            }
        }
    }

    @Parcelize
    data class Input(val title: String, val text: String, val receiveAtLeast: String) : Parcelable
}

@Composable
fun MaxSlippageInfoScreen(title: String?, text: String?, receiveAtLeast: String, onCloseClick: () -> Unit) {
    BottomSheetHeader(
        iconPainter = painterResource(R.drawable.ic_info_24),
        iconTint = ColorFilter.tint(ComposeAppTheme.colors.grey),
        title = title ?: "",
        onCloseClick = onCloseClick
    ) {
        Column(modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp)) {
            Text(
                text = text ?: "",
                style = ComposeAppTheme.typography.body,
                color = ComposeAppTheme.colors.bran
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    painterResource(R.drawable.icon_warning_2_20),
                    modifier = Modifier.size(18.dp),
                    tint = if(isSystemInDarkTheme()) Yellow50 else Yellow80,
                    contentDescription = "warning"
                )
                HSpacer(8.dp)
                Text(
                    text = stringResource(R.string.slippage_may_be_higher_than_necessary),
                    style = ComposeAppTheme.typography.subhead2,
                    fontWeight = FontWeight.SemiBold,
                    color = if(isSystemInDarkTheme()) Yellow50 else Yellow80
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = ComposeAppTheme.colors.jeremy, shape = RoundedCornerShape(8.dp))
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    modifier = Modifier.weight(0.3f),
                    text = stringResource(R.string.receive_at_least),
                    style = ComposeAppTheme.typography.subhead1,
                    color = ComposeAppTheme.colors.bran
                )
                HSpacer(8.dp)
                Text(
                    modifier = Modifier.weight(0.7f),
                    text = receiveAtLeast,
                    style = ComposeAppTheme.typography.subhead1,
                    color = ComposeAppTheme.colors.bran
                )
            }
        }


    }
}
