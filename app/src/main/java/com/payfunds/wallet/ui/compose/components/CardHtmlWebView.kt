package com.payfunds.wallet.ui.compose.components

import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import io.github.alexzhirkevich.qrose.options.roundCorners

@Composable
fun CardHtmlWebView(
    url: String,
    modifier:  Modifier = Modifier
        .padding(horizontal = 16.dp)
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
) {
    AndroidView(
        factory = { context ->
            WebView(context).apply {
                // 1. Force the WebView to match the internal parent container
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                webViewClient = WebViewClient()

                settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true

                    // 2. These two flags enable the "Zoom to Fit" behavior
                    // Makes the WebView think it has a large viewport...
                    useWideViewPort = true
                    // ...and then zooms it out to fit the screen width.
                    loadWithOverviewMode = true

                    // Optional: Disable manual pinch-zoom if you want the card to stay fixed
                    setSupportZoom(false)
                }
                loadUrl(url)
            }
        },
        modifier = modifier
    )
}
