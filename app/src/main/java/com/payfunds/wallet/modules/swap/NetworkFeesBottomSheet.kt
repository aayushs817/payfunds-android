package com.payfunds.wallet.modules.swap

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.payfunds.wallet.R
import com.payfunds.wallet.ui.compose.ComposeAppTheme
import com.payfunds.wallet.ui.compose.components.HsIconButton
import com.payfunds.wallet.ui.compose.components.VSpacer
import com.payfunds.wallet.ui.compose.components.headline2_leah
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkFeesBottomSheet(
    title: String,
    text: String,
    onSheetStateChange: (Boolean) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = { onSheetStateChange(false) },
        sheetState = sheetState,
        containerColor = ComposeAppTheme.colors.lawrence,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = ComposeAppTheme.colors.jeremy
            )
        },// Set the background color
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp, 24.dp, 0.dp, 0.dp))
                .verticalScroll(rememberScrollState())
                .background(color = ComposeAppTheme.colors.lawrence)
        ) {
            Row(
                modifier = Modifier
                    .padding(start = 32.dp, top = 8.dp, end = 32.dp, bottom = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    modifier = Modifier.size(24.dp),
                    painter = painterResource(R.drawable.ic_info_24),
                    colorFilter = ColorFilter.tint(ComposeAppTheme.colors.grey),
                    contentDescription = null
                )
                headline2_leah(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .weight(1f)
                        .align(Alignment.CenterVertically),
                    text = title,
                    maxLines = 1,
                )
                HsIconButton(
                    modifier = Modifier.size(24.dp),
                    onClick = {
                        coroutineScope.launch { sheetState.hide() }.invokeOnCompletion {
                            if (!sheetState.isVisible) {
//                            isSheetOpen = false
                                onSheetStateChange(false)
                            }
                        }
                    }
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_close),
                        tint = ComposeAppTheme.colors.grey,
                        contentDescription = null,
                    )
                }
            }
            Column(modifier = Modifier.padding(horizontal = 32.dp, vertical = 8.dp)) {
                Text(
                    text = text,
                    style = ComposeAppTheme.typography.body,
                    color = ComposeAppTheme.colors.bran
                )
                VSpacer(55.dp)
            }


        }
    }
}

