package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

@Composable
fun NujoomGradientBg(
    isChildUI: Boolean = false,
    content: @Composable BoxScope.() -> Unit
) {
    val brush = if (isChildUI) {
        Brush.linearGradient(
            colors = listOf(ChildBgStart, ChildBgEnd)
        )
    } else {
        Brush.verticalGradient(
            colors = listOf(BgDarkPrimary, BgDarkSecondary)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush)
            .windowInsetsPadding(WindowInsets.statusBars),
        content = content
    )
}

@Composable
fun NujoomButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = true,
    testTag: String = ""
) {
    val containerColor = if (isPrimary) AccentOrange else Color.Transparent
    val contentColor = if (isPrimary) Color.White else AccentGold
    val borderStroke = if (isPrimary) null else BorderStroke(1.5.dp, AccentGold)

    Button(
        onClick = onClick,
        colors = ButtonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = Color.Gray.copy(alpha = 0.5f),
            disabledContentColor = Color.White.copy(alpha = 0.5f)
        ),
        shape = RoundedCornerShape(24.dp),
        border = borderStroke,
        modifier = modifier
            .height(52.dp)
            .testTag(testTag),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(
            text = text,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun StarBalanceWidget(
    stars: Int,
    modifier: Modifier = Modifier,
    label: String = "ستار"
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .padding(12.dp)
            .background(
                Brush.radialGradient(
                    colors = listOf(AccentGold.copy(alpha = 0.25f), Color.Transparent)
                )
            )
            .padding(24.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "⭐",
                fontSize = 52.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
            Text(
                text = "$stars",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = AccentGold,
                textAlign = TextAlign.Center
            )
            Text(
                text = label,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = BgLightPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MiniStarCounter(
    stars: Int,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(BgDarkSecondary)
            .border(1.dp, BgDarkCardBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = "⭐ $stars",
            color = AccentGold,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp
        )
    }
}

@Composable
fun NujoomCard(
    modifier: Modifier = Modifier,
    borderColor: Color = BgDarkCardBorder,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val cardModifier = if (onClick != null) {
        modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
    } else {
        modifier
    }

    Column(
        modifier = cardModifier
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .background(BgDarkCard)
            .padding(16.dp),
        content = content
    )
}

@Composable
fun CircularProgressRing(
    percentage: Float,
    text: String,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp
) {
    val color = when {
        percentage < 34f -> ErrorRed
        percentage < 67f -> WarningAmber
        percentage < 100f -> InfoBlue
        else -> SuccessGreen
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(size)
    ) {
        CircularProgressIndicator(
            progress = { percentage / 100f },
            modifier = Modifier.fillMaxSize(),
            color = color,
            strokeWidth = strokeWidth,
            trackColor = BgDarkCardBorder,
        )
        Text(
            text = text,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun NujoomTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    isPassword: Boolean = false,
    testTag: String = ""
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = { Text(placeholder, color = Color.Gray) },
        singleLine = true,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = AccentPurple,
            unfocusedBorderColor = BgDarkCardBorder,
            focusedTextColor = Color.White,
            unfocusedTextColor = Color.White,
            focusedContainerColor = BgDarkCard,
            unfocusedContainerColor = BgDarkCard
        ),
        modifier = modifier
            .fillMaxWidth()
            .testTag(testTag)
    )
}

@Composable
fun EmptyStateWidget(
    emoji: String,
    title: String,
    description: String,
    onActionClick: (() -> Unit)? = null,
    actionText: String = ""
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(emoji, fontSize = 56.sp, modifier = Modifier.padding(bottom = 12.dp))
        Text(
            title,
            fontSize = 19.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 6.dp)
        )
        Text(
            description,
            fontSize = 14.sp,
            color = Color.Gray,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        if (onActionClick != null && actionText.isNotEmpty()) {
            NujoomButton(text = actionText, onClick = onActionClick, isPrimary = false)
        }
    }
}
