package com.payfunds.wallet.ui.compose.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.payfunds.wallet.R
import com.payfunds.wallet.ui.compose.ComposeAppTheme

@Composable
fun CreditCardView(
    cardNumber: String,
    expiryDate: String,
    cvv: String,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF42080D), // Dark Red/Blackish
                        Color(0xFF7D1220), // Deep Red
                        Color(0xFFB71C1C)  // Brighter Red
                    ),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
            .padding(horizontal = 24.dp)
            .padding(top = 24.dp)
    ) {
        // Decorative background curves can go here if needed, keeping it simple for now as requested.
        
        Column(modifier = Modifier.fillMaxSize()) {
            // Top Row: Logo and Eye
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // PayFUNDS Logo Area
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_payfunds_main),
                        contentDescription = "PayFunds Logo",
                        tint = Color.Unspecified, // Assuming the drawable has its own colors, or use White if mono
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PayFUNDS",
                        style = ComposeAppTheme.typography.title3,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Icon(
                    painter = painterResource(id = if (isVisible) R.drawable.ic_eye_20 else R.drawable.ic_eye_off_20),
                    contentDescription = if (isVisible) "Hide details" else "Show details",
                    tint = Color.White.copy(alpha = 0.8f),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { isVisible = !isVisible }
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            // Middle: Card Number
            Text(
                text = if (isVisible) cardNumber else maskCardNumber(cardNumber),
                style = ComposeAppTheme.typography.title3,
                color = Color.White,
                fontSize = 20.sp,
                letterSpacing = 4.sp,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Details Row: Expiry and CVV
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Column {
                    Text(
                        text = "Expiration Date",
                        style = ComposeAppTheme.typography.caption,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isVisible) expiryDate else "**/**",
                        style = ComposeAppTheme.typography.body,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(32.dp))

                Column {
                    Text(
                        text = "Security Code",
                        style = ComposeAppTheme.typography.caption,
                        color = Color.White.copy(alpha = 0.7f),
                        fontSize = 10.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isVisible) cvv else "***",
                        style = ComposeAppTheme.typography.body,
                        color = Color.White,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        modifier = Modifier.align(Alignment.Start)
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Bottom Row: Name and Visa Logo
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                // Visa Logo Styles
                Image(
                    painter = painterResource(id = R.drawable.ic_visa),
                    contentDescription = "VISA",
                    modifier = Modifier.size(60.dp)
                )
            }
        }
    }
}

private fun maskCardNumber(cardNumber: String): String {
    val cleanNumber = cardNumber.replace(" ", "")
    if (cleanNumber.length < 4) return "****"
    val last4 = cleanNumber.takeLast(4)
    return "**** **** **** $last4"
}
