package com.example.ui.screens

import android.os.Handler
import android.os.Looper
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.NujoomViewModel
import java.text.SimpleDateFormat
import java.util.*

private fun parseHexColor(hex: String, fallback: Color = Color(0xFFFFB800)): Color {
    return try {
        val cleanHex = hex.trim().removePrefix("#")
        if (cleanHex.length == 6) {
            Color((0xFF000000 or cleanHex.toLong(16)).toInt())
        } else if (cleanHex.length == 8) {
            Color(cleanHex.toLong(16).toInt())
        } else {
            fallback
        }
    } catch (e: Exception) {
        fallback
    }
}

@Composable
fun ChildHomeScreen(
    vm: NujoomViewModel
) {
    val context = LocalContext.current
    val lang by vm.currentLanguage.collectAsState()
    val activeChild by vm.childSessionData.collectAsState()
    val tasks by vm.activeChildTasks.collectAsState()
    val messages by vm.childInboxMessages.collectAsState()
    val familyLeaderboardEnabled by vm.familyLeaderboardEnabled.collectAsState()
    val siblings by vm.childrenList.collectAsState()

    // Camera Simulation State
    var showCameraSimulation by remember { mutableStateOf(false) }
    var activeCameraTaskId by remember { mutableStateOf("") }
    var cameraPhotoTaken by remember { mutableStateOf(false) }

    // Message Inbox viewstate dialog
    var viewingMessageText by remember { mutableStateOf<String?>(null) }

    if (showCameraSimulation) {
        AlertDialog(
            onDismissRequest = { showCameraSimulation = false },
            containerColor = BgDarkSecondary,
            title = { Text("التقط إثبات الصورة دليلاً! 📸", color = Color.White) },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("التقط صورة وأنت تمسك بفرشاة أسنانك أو ترتّب سريرك", color = Color.LightGray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(16.dp))

                    if (!cameraPhotoTaken) {
                        // Simulated camera lens viewfinder
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color.Black)
                                .border(2.dp, AccentPurpleMid, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("🎥 [عدسة الكاميرا النشطة]", color = Color.Gray, fontSize = 11.sp)
                                Text("انقر لالتقاط الدليل الفوري", color = AccentGold, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    } else {
                        // Taken state preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(200.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(SuccessGreen.copy(alpha = 0.2f))
                                .border(2.dp, SuccessGreen, RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("✅ تم التقاط الدليل المصوّر بنجاح!", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("صورة مفرشة/مرتبّة واضحة بنسبة 100%", color = Color.LightGray, fontSize = 12.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                if (!cameraPhotoTaken) {
                    NujoomButton(
                        text = "التقط الصورة 📸",
                        onClick = { cameraPhotoTaken = true }
                    )
                } else {
                    NujoomButton(
                        text = "إرسال للمراجعة 📤",
                        onClick = {
                            vm.submitTaskProof(activeCameraTaskId)
                            showCameraSimulation = false
                            cameraPhotoTaken = false
                            Toast.makeText(context, "📨 تم الإرسال بنجاح! انتظر موافقة والديك لتستلم نجومك ✨!", Toast.LENGTH_LONG).show()
                        }
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showCameraSimulation = false
                    cameraPhotoTaken = false
                }) {
                    Text("إلغاء", color = Color.Gray)
                }
            }
        )
    }

    if (viewingMessageText != null) {
        AlertDialog(
            onDismissRequest = { viewingMessageText = null },
            containerColor = BgDarkCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💌", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(vm.translate("encouraging_message_title"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = { Text(viewingMessageText!!, color = Color.White, fontSize = 16.sp, textAlign = TextAlign.Center) },
            confirmButton = {
                NujoomButton(text = "شكراً يا والدي المحب ❤️", onClick = { viewingMessageText = null })
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentPadding = PaddingValues(bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Child identity top panel
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val frameColor = "#FFB800"
                    val av = NujoomConstants.AVATARS.firstOrNull { it.id == activeChild?.avatarId }?.emoji ?: "🤓"
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(parseHexColor(frameColor).copy(alpha = 0.2f))
                            .border(2.5.dp, parseHexColor(frameColor), CircleShape)
                    ) {
                        Text(av, fontSize = 32.sp)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text("مرحباً بك يا بطل! 👋", color = Color.Gray, fontSize = 12.sp)
                        Text(activeChild?.firstName.orEmpty(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (activeChild != null && activeChild!!.streak > 1) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = AccentOrange.copy(alpha = 0.15f)),
                            border = BorderStroke(1.dp, AccentOrange),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "🔥 ${activeChild?.streak} د!",
                                color = AccentOrange,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                    }

                    Card(
                        modifier = Modifier
                            .clickable { vm.logOut() }
                            .padding(2.dp),
                        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, ErrorRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "🚪 خروج",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }

        // Star Counter Widget
        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                StarBalanceWidget(
                    stars = activeChild?.starBalance ?: 0,
                    label = "نجومي الحالية 🌟"
                )
                
                // Add Lifetime Points tracker
                val lifetimePoints = activeChild?.totalStarsEarned ?: 0
                
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = BgDarkCard),
                    border = BorderStroke(1.2.dp, BgDarkCardBorder),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("🎯", fontSize = 22.sp, modifier = Modifier.padding(end = 8.dp))
                                Column {
                                    Text("نقاطي التراكمية 🌟", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    Text("تبدأ من الصفر وتزداد كجائزة مع كل تميز!", color = Color.Gray, fontSize = 10.sp)
                                }
                            }
                            Text(
                                text = "$lifetimePoints نقطة",
                                color = AccentGold,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 18.sp
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // Mini progress or visual milestone
                        val nextMilestone = if (lifetimePoints < 10) 10 else if (lifetimePoints < 50) 50 else if (lifetimePoints < 100) 100 else 500
                        val progressPercent = (lifetimePoints.toFloat() / nextMilestone).coerceIn(0f, 1f)
                        
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("المرحلة القادمة: $nextMilestone نجمة", color = Color.LightGray, fontSize = 11.sp)
                                Text("${(progressPercent * 100).toInt()}%", color = AccentGold, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            // Custom progress bar
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(10.dp)
                                    .clip(CircleShape)
                                    .background(BgDarkCardBorder)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(progressPercent)
                                        .clip(CircleShape)
                                        .background(
                                            Brush.horizontalGradient(
                                                colors = listOf(AccentGold, AccentOrange)
                                            )
                                        )
                                )
                            }
                        }
                    }
                }
            }
        }

        // Achievements/Badges Grid Box
        item {
            val earnedBadges = activeChild?.badges?.split(",")?.filter { it.isNotEmpty() }?.toSet().orEmpty()
            val allBadges = NujoomConstants.ACHIEVEMENT_BADGES
            
            var selectedBadgeForDetail by remember { mutableStateOf<AchievementBadge?>(null) }
            
            if (selectedBadgeForDetail != null) {
                val b = selectedBadgeForDetail!!
                val unlocked = earnedBadges.contains(b.id)
                AlertDialog(
                    onDismissRequest = { selectedBadgeForDetail = null },
                    containerColor = BgDarkSecondary,
                    shape = RoundedCornerShape(20.dp),
                    title = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                                    .background(
                                        parseHexColor(b.color).copy(alpha = if (unlocked) 0.25f else 0.05f)
                                    )
                                    .border(
                                        2.dp, 
                                        if (unlocked) parseHexColor(b.color) else Color.Gray.copy(alpha = 0.5f), 
                                        CircleShape
                                    )
                            ) {
                                Text(if (unlocked) b.emoji else "🔒", fontSize = 34.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(b.nameAr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                            Text(b.nameEn, color = Color.Gray, fontSize = 12.sp)
                        }
                    },
                    text = {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = b.descriptionAr,
                                color = Color.White,
                                fontSize = 14.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                            Text(
                                text = b.descriptionEn,
                                color = Color.LightGray,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            
                            // Status bar indicator
                            if (unlocked) {
                                Text("🎉 تم فتح هذا الإنجاز بنجاح!", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            } else {
                                val childTotalStars = activeChild?.totalStarsEarned ?: 0
                                val childStreak = activeChild?.streak ?: 0
                                
                                val triggerReq = when (b.triggerType) {
                                    "total-stars" -> "مجموع النجوم المطلوب: ${b.triggerValue} (لديك: $childTotalStars)"
                                    "streak" -> "الأيام المتواصلة المطلوبة: ${b.triggerValue} (لديك: $childStreak)"
                                    "recitation-count" -> "تلاوات قرآنية مطلوبة: ${b.triggerValue}"
                                    else -> "إتمام شروط خاصة: ${b.triggerValue}"
                                }
                                Text("🔒 مغلق حالياً", color = ErrorRed, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(triggerReq, color = Color.Gray, fontSize = 11.sp, textAlign = TextAlign.Center)
                            }
                        }
                    },
                    confirmButton = {
                        NujoomButton(
                            text = "رائع 👍", 
                            onClick = { 
                                selectedBadgeForDetail = null
                                NujoomSoundPlayer.playClickSound()
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                )
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                colors = CardDefaults.cardColors(containerColor = BgDarkCard),
                border = BorderStroke(1.2.dp, BgDarkCardBorder),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🏅 حائط الإنجازات والأوسمة:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(
                            text = "${earnedBadges.size} / ${allBadges.size} مفتوح",
                            color = AccentGold,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Simple grid representation using Row of Cards or vertical list
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        allBadges.chunked(3).forEach { chunk ->
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                chunk.forEach { badge ->
                                    val isEarned = earnedBadges.contains(badge.id)
                                    val bdColor = parseHexColor(badge.color)
                                    
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .clickable {
                                                selectedBadgeForDetail = badge
                                                NujoomSoundPlayer.playClickSound()
                                            },
                                        colors = CardDefaults.cardColors(
                                            containerColor = if (isEarned) bdColor.copy(alpha = 0.1f) else BgDarkSecondary.copy(alpha = 0.4f)
                                        ),
                                        border = BorderStroke(
                                            1.dp,
                                            if (isEarned) bdColor else BgDarkCardBorder
                                        ),
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center,
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Box(
                                                contentAlignment = Alignment.Center,
                                                modifier = Modifier
                                                    .size(40.dp)
                                                    .clip(CircleShape)
                                                    .background(if (isEarned) bdColor.copy(alpha = 0.2f) else Color.DarkGray.copy(alpha = 0.3f))
                                            ) {
                                                Text(if (isEarned) badge.emoji else "🔒", fontSize = 22.sp)
                                            }
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = badge.nameAr,
                                                color = if (isEarned) Color.White else Color.Gray,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 11.sp,
                                                maxLines = 1,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                }
                                // Pad empty columns in chunk
                                if (chunk.size < 3) {
                                    repeat(3 - chunk.size) {
                                        Spacer(modifier = Modifier.weight(1f))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Parent Message Inbox alerts
        val unreadMsg = messages.filter { !it.isRead }
        if (unreadMsg.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            val msg = unreadMsg.first()
                            viewingMessageText = msg.content
                            vm.markMessageAsRead(msg.messageId)
                        },
                    colors = CardDefaults.cardColors(containerColor = AccentPurple.copy(alpha = 0.15f)),
                    border = BorderStroke(1.2.dp, AccentPurple)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("💌", fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                        Text(
                            text = "لديك رسالة تشجيعية عذبة من والديك! انقر لقراءتها 😍",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // Sibling Leaderboard (If enabled)
        if (familyLeaderboardEnabled) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BgDarkCard),
                    border = BorderStroke(1.dp, BgDarkCardBorder)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("🏆 ترتيب نجوم العائلة:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        val rankedSiblings = siblings.sortedByDescending { it.starBalance }
                        rankedSiblings.forEachIndexed { idx, sib ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val rankSymbol = when(idx) {
                                    0 -> "🥇"
                                    1 -> "🥈"
                                    2 -> "🥉"
                                    else -> "⭐"
                                }
                                Text("$rankSymbol ${sib.firstName}", color = Color.White, fontSize = 13.sp)
                                Text("${sib.starBalance} نجمة", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        }

        // Task header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text("مهامي اليومية 🎯:", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }

        // Child Task Cards
        if (tasks.isEmpty()) {
            item {
                EmptyStateWidget(
                    emoji = "🎯",
                    title = "لا توجد مهام اليوم",
                    description = "استرح أو اطلب من والديك إضافة مهام رائعة لتبدأ بجمع النجوم اليوم!"
                )
            }
        } else {
            items(tasks) { task ->
                NujoomCard {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(task.emoji, fontSize = 32.sp, modifier = Modifier.padding(end = 12.dp))
                            Column {
                                Text(
                                    text = if (lang == "ar") task.nameAr else task.nameEn,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "مكافأة: +⭐ ${task.starsReward} نجمة",
                                    color = AccentGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }

                        // Child action context
                        when (task.status) {
                            "completed" -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SuccessGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("منجزة ✅", color = SuccessGreen, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            "awaiting-review" -> {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(AccentOrange.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("بانتظار الوالد ⏳", color = AccentOrange, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                            "rejected" -> {
                                NujoomButton(
                                    text = "أعد الإثبات 🔄",
                                    onClick = {
                                        activeCameraTaskId = task.taskId
                                        showCameraSimulation = true
                                    },
                                    modifier = Modifier.height(36.dp)
                                )
                            }
                            else -> {
                                val buttonText = if (task.requiresProof) "أرسل دليل 📸" else "أنهيت! ✓"
                                NujoomButton(
                                    text = buttonText,
                                    onClick = {
                                        if (task.requiresProof) {
                                            activeCameraTaskId = task.taskId
                                            showCameraSimulation = true
                                        } else {
                                            vm.completeDirectTask(task.taskId)
                                            Toast.makeText(context, "🎉 مبارك! كسبت +${task.starsReward} نجمة!", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.height(36.dp),
                                    isPrimary = task.requiresProof
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// Muslim prayers tracker panel
@Composable
fun ChildPrayersScreen(
    vm: NujoomViewModel
) {
    val context = LocalContext.current
    val lang by vm.currentLanguage.collectAsState()
    val activePrayers by vm.activeChildPrayers.collectAsState()

    var showConfirmDialog by remember { mutableStateOf<String?>(null) }

    if (showConfirmDialog != null) {
        val pKey = showConfirmDialog!!
        val arabicName = when(pKey) {
            "fajr" -> vm.translate("fajr")
            "dhuhr" -> vm.translate("dhuhr")
            "asr" -> vm.translate("asr")
            "maghrib" -> vm.translate("maghrib")
            else -> vm.translate("isha")
        }
        AlertDialog(
            onDismissRequest = { showConfirmDialog = null },
            containerColor = BgDarkSecondary,
            title = { Text("تأكيد الصلاة 🕌", color = Color.White) },
            text = { Text("هل تمكنت من صلاة $arabicName في وقتها بخشوع وحضور؟", color = Color.LightGray) },
            confirmButton = {
                NujoomButton(
                    text = "نعم، صلّيت! ✅",
                    onClick = {
                        vm.markPrayerCompleted(pKey)
                        showConfirmDialog = null
                        Toast.makeText(context, "تقبل الله طاعتك يا بطل! كسبت +1 نجمة ⭐!", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = null }) {
                    Text("ليس بعد", color = Color.Gray)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("صلاتي وقرآني 📿", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("حافظ على الصلوات الخمس ورتل القرآن لتزيد نجومك 🕌", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)

        val prayersMap = listOf(
            Triple("fajr", vm.translate("fajr"), activePrayers?.fajr ?: false),
            Triple("dhuhr", vm.translate("dhuhr"), activePrayers?.dhuhr ?: false),
            Triple("asr", vm.translate("asr"), activePrayers?.asr ?: false),
            Triple("maghrib", vm.translate("maghrib"), activePrayers?.maghrib ?: false),
            Triple("isha", vm.translate("isha"), activePrayers?.isha ?: false),
        )

        prayersMap.forEach { (key, arabicLabel, done) ->
            NujoomCard(
                borderColor = if (done) SuccessGreen else BgDarkCardBorder
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🌙", fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                        Column {
                            Text(arabicLabel, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("الفريضة اليومية", color = Color.Gray, fontSize = 12.sp)
                        }
                    }

                    if (done) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(SuccessGreen.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text("صلّيت ✅ +⭐1", color = SuccessGreen, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        NujoomButton(
                            text = "صلّيت! ✓",
                            onClick = { showConfirmDialog = key },
                            modifier = Modifier.height(36.dp)
                        )
                    }
                }
            }
        }
    }
}

// Quran Surah Al Mulk wave recorder simulator console
@Composable
fun ChildQuranScreen(
    vm: NujoomViewModel
) {
    val context = LocalContext.current
    val recitations by vm.activeChildRecitations.collectAsState()
    val child by vm.childSessionData.collectAsState()

    val currentSurah = child?.memorizeSurah ?: "سورة الملك"
    val targetAyas = child?.memorizeAyahs ?: 5

    val activeSurahInfo = NujoomConstants.QURAN_SURAHS.find { it.nameAr == currentSurah || it.nameEn == currentSurah }
    val totalAyasInSurah = activeSurahInfo?.ayahs ?: 30

    var recordingState by remember { mutableStateOf(false) } // false: idle, true: recording
    var recordingComplete by remember { mutableStateOf(false) }
    var selectedSheikh by remember { mutableStateOf<String?>("الشيخ مشاري بن راشد العفاسي") }

    val sheikhs = listOf(
        Pair("الشيخ عبد الباسط عبد الصمد", "🎙️"),
        Pair("الشيخ مشاري بن راشد العفاسي", "🎵"),
        Pair("الشيخ محمود خليل الحصري", "🕌")
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("تلاوة القرآن الكريم 📖", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgDarkCard),
            border = BorderStroke(1.2.dp, AccentGold)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(currentSurah, color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                Text("$totalAyasInSurah آية 🕌", color = Color.Gray, fontSize = 12.sp)

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Minus Button
                    Card(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable {
                                if (targetAyas > 1) {
                                    vm.adjustMemorizeAyahs(-1)
                                    NujoomSoundPlayer.playClickSound()
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = BgDarkCardBorder),
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("-", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "حفظ $targetAyas آيات",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "المستهدفة هذا اليوم",
                            color = Color.LightGray,
                            fontSize = 11.sp
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    // Plus Button
                    Card(
                        modifier = Modifier
                            .size(36.dp)
                            .clickable {
                                if (targetAyas < totalAyasInSurah) {
                                    vm.adjustMemorizeAyahs(1)
                                    NujoomSoundPlayer.playClickSound()
                                }
                            },
                        colors = CardDefaults.cardColors(containerColor = BgDarkCardBorder),
                        shape = CircleShape
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Text("+", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // Recorder simulation widget
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgDarkCard),
            border = BorderStroke(1.dp, BgDarkCardBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (!recordingState && !recordingComplete) {
                    Text("🎙️ مستعد لتسجيل تلاوتك الخاشعة؟", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            recordingState = true
                            NujoomSoundPlayer.playClickSound()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                        modifier = Modifier.size(72.dp),
                        shape = CircleShape
                    ) {
                        Text("🎙️", fontSize = 28.sp)
                    }
                } else if (recordingState) {
                    Text("🔴 جاري تسجيل تلاوتك الآن...", color = ErrorRed, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))
                    // Waves lines simulator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(40.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val pulse = listOf(0.3f, 0.8f, 0.5f, 0.9f, 0.4f, 0.7f, 0.3f)
                        pulse.forEach { h ->
                            Box(
                                modifier = Modifier
                                    .padding(horizontal = 3.dp)
                                    .width(4.dp)
                                    .fillMaxHeight(h)
                                    .background(AccentPurple)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            recordingState = false
                            recordingComplete = true
                            NujoomSoundPlayer.playUpSound()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                        modifier = Modifier.height(42.dp)
                    ) {
                        Text("⏹️ إيقاف التسجيل", color = Color.White)
                    }
                } else {
                    Text("🎵 تم التسجيل بنجاح!", color = SuccessGreen, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "شارك تلاوتك مباشرة لتصحيحها ومراجعتها عبر الواتساب على الرقم: 01001853928",
                        color = Color.White,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row {
                        NujoomButton(
                            text = "إرسال عبر واتساب 💬",
                            onClick = {
                                vm.submitRecitation(currentSurah, "0:45", "families/NJ-SIMULATED/recitations/rec_test.m4a", "المصحح عبر واتساب")
                                recordingComplete = false
                                NujoomSoundPlayer.playSuccessSound()
                                try {
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/201001853928?text=السلام%20عليكم،%20لقد%20سجلت%20تلاوة%20جديدة%20لحفظ%20$targetAyas%20آيات%20من%20$currentSurah%20عبر%20تطبيق%20نجوم!"))
                                    context.startActivity(intent)
                                } catch (e: Exception) {
                                    Toast.makeText(context, "لم يتم العثور على تطبيق واتساب", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        TextButton(onClick = {
                            recordingComplete = false
                            NujoomSoundPlayer.playClickSound()
                        }) {
                            Text("أعد التسجيل", color = Color.Gray)
                        }
                    }
                }
            }
        }

        // Quran History logs
        Text("تلاواتي السابقة والسور المسجلة:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        if (recitations.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = BgDarkCard)
            ) {
                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                    Text("لم تسجّل تلاوتك المفصلة اليوم 🤔", color = Color.Gray)
                }
            }
        } else {
            recitations.forEach { rc ->
                NujoomCard {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(4.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(rc.surahName, color = Color.White, fontWeight = FontWeight.Bold)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("المدة: ٤٥ ثانية", color = Color.Gray, fontSize = 11.sp)
                                    if (rc.sheikhName != null) {
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(AccentPurple.copy(alpha = 0.2f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text("التصحيح", color = AccentPurpleMid, fontSize = 10.sp)
                                        }
                                    }
                                }
                            }

                            if (rc.sheikhComment != null) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(SuccessGreen.copy(alpha = 0.2f))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("تم التقييم ⭐ +${rc.starsAwarded}", color = SuccessGreen, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            } else if (rc.sheikhName != null) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(AccentGold.copy(alpha = 0.15f))
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Text("بانتظار التقييم ⏳", color = AccentGold, fontSize = 11.sp)
                                        }
                            } else {
                                Text("قيد المراجعة ⏳", color = Color.Gray, fontSize = 12.sp)
                            }
                        }

                        if (rc.sheikhComment != null) {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = BgDarkCard.copy(alpha = 0.6f)),
                                border = BorderStroke(1.dp, BgDarkCardBorder)
                            ) {
                                Column(
                                    modifier = Modifier.padding(10.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "💬 نصيحة وتقييم المصحح:",
                                            color = AccentGold,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        // Display star rate row based on rc.starsAwarded
                                        Row {
                                            repeat(rc.starsAwarded) {
                                                Text("⭐", fontSize = 12.sp)
                                            }
                                        }
                                    }
                                    Text(
                                        text = rc.sheikhComment,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        lineHeight = 18.sp
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// Child rewards shop widget representation
@Composable
fun ChildRewardsScreen(
    vm: NujoomViewModel
) {
    val context = LocalContext.current
    val lang by vm.currentLanguage.collectAsState()
    val activeChild by vm.childSessionData.collectAsState()
    val rewards by vm.activeFamilyRewards.collectAsState()

    var showChoiceReceipt by remember { mutableStateOf<String?>(null) }

    if (showChoiceReceipt != null) {
        val rId = showChoiceReceipt!!
        val award = rewards.firstOrNull { it.rewardId == rId }
        val name = if (lang == "ar") award?.nameAr.orEmpty() else award?.nameEn.orEmpty()
        val cost = award?.starsCost ?: 15

        AlertDialog(
            onDismissRequest = { showChoiceReceipt = null },
            containerColor = BgDarkSecondary,
            title = { Text("تبديل جائزتك الرائجة! 🍿", color = Color.White) },
            text = { Text("هل تود استبدال $name من رصيد نجومك مقابل ⭐ $cost نجمة؟", color = Color.LightGray) },
            confirmButton = {
                NujoomButton(
                    text = "نعم، أريدها الآن! 🎉",
                    onClick = {
                        vm.requestRedemption(rId)
                        showChoiceReceipt = null
                        Toast.makeText(context, "تم إرسال طلب الترشح بنجاح! بانتظار إفادة الأهل!", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showChoiceReceipt = null }) {
                    Text("ليس الآن", color = Color.Gray)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("متجر النجوم السعيدة 🌟🛍️", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        Text("استبدل نجومك التي جمعتها بالجد والمثابرة بمكافآت ممتعة:", color = Color.Gray, fontSize = 13.sp, textAlign = TextAlign.Center)

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(AccentPurple.copy(alpha = 0.2f))
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("رصيدك الحالي المتاح: ⭐ ${activeChild?.starBalance ?: 0} نجمة", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }

        if (rewards.isEmpty()) {
            EmptyStateWidget(
                emoji = "🎁",
                title = "المتجر فارغ حالياً",
                description = "لم يضف والديك مكافآت للمتجر بعد. اطلب منهم إضافة كرات آيس كريم أو تذاكر لعب!"
            )
        } else {
            rewards.filter { it.isAvailable }.forEach { rw ->
                val balance = activeChild?.starBalance ?: 0
                val canAfford = balance >= rw.starsCost

                NujoomCard(
                    borderColor = if (canAfford) AccentGold.copy(alpha = 0.6f) else BgDarkCardBorder
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(rw.emoji, fontSize = 36.sp, modifier = Modifier.padding(end = 12.dp))
                            Column {
                                Text(
                                    text = if (lang == "ar") rw.nameAr else rw.nameEn,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "التكلفة: ⭐ ${rw.starsCost} نجمة",
                                    color = AccentGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (canAfford) {
                            NujoomButton(
                                text = "استقبل الجائزة! ✨",
                                onClick = { showChoiceReceipt = rw.rewardId },
                                modifier = Modifier.height(36.dp)
                            )
                        } else {
                            val missing = rw.starsCost - balance
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Gray.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("تحتاج $missing ⭐ أخرى", color = Color.Gray, fontSize = 11.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// Child Portal personal options
@Composable
fun ChildSettingsScreen(
    vm: NujoomViewModel
) {
    val activeChild by vm.childSessionData.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("⚙️ خيارات الملف الشخصي والألوان", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgDarkCard),
            border = BorderStroke(1.dp, BgDarkCardBorder)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                val frameColor = "#FFB800"
                val av = NujoomConstants.AVATARS.firstOrNull { it.id == activeChild?.avatarId }?.emoji ?: "🤓"
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(parseHexColor(frameColor).copy(alpha = 0.2f))
                        .border(3.dp, parseHexColor(frameColor), CircleShape)
                ) {
                    Text(av, fontSize = 42.sp)
                }

                Text("${activeChild?.firstName.orEmpty()} ${activeChild?.lastName.orEmpty()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text("المستوى الإيماني والهمة العالية ⭐", color = AccentGold, fontSize = 13.sp)
            }
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgDarkCard),
            border = BorderStroke(1.dp, BgDarkCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {
                Text("تخصيص الخصائص والتحفيز:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مؤثرات الصوت السعيدة 🎉", color = Color.White, fontSize = 13.sp)
                    Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = AccentOrange))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("اهتزاز التشجيع التفاعلي 📳", color = Color.White, fontSize = 13.sp)
                    Switch(checked = true, onCheckedChange = {}, colors = SwitchDefaults.colors(checkedThumbColor = AccentOrange))
                }
            }
        }

        NujoomButton(
            text = "تسجيل الخروج 🚪",
            onClick = { vm.logOut() },
            modifier = Modifier.fillMaxWidth(),
            testTag = "child_logout_btn"
        )
    }
}
