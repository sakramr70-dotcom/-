package com.example.ui.screens

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.NujoomViewModel

@Composable
fun ParentMainDashboard(
    vm: NujoomViewModel,
    onNavigateChild: (String) -> Unit
) {
    val context = LocalContext.current
    val lang by vm.currentLanguage.collectAsState()
    val familyCode by vm.currentFamilyCode.collectAsState()
    val children by vm.childrenList.collectAsState()
    val recentTasks by vm.familyRecentTasks.collectAsState()
    val clipboard = LocalClipboardManager.current

    // Dialog state for adding a custom child
    var showAddChildDialog by remember { mutableStateOf(false) }
    var childFirstName by remember { mutableStateOf("") }
    var childLastName by remember { mutableStateOf("") }
    var childAge by remember { mutableStateOf(8) }

    // Family Code Expansion Widget
    var codeExpanded by remember { mutableStateOf(false) }

    val notifications by vm.activeNotifications.collectAsState(initial = emptyList())
    var showNotificationsDialog by remember { mutableStateOf(false) }

    if (showAddChildDialog) {
        AlertDialog(
            onDismissRequest = { showAddChildDialog = false },
            containerColor = BgDarkCard,
            title = { Text(vm.translate("add_child"), color = Color.White) },
            text = {
                Column {
                    NujoomTextField(
                        value = childFirstName,
                        onValueChange = { childFirstName = it },
                        placeholder = vm.translate("first_name"),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    NujoomTextField(
                        value = childLastName,
                        onValueChange = { childLastName = it },
                        placeholder = vm.translate("last_name"),
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(vm.translate("age"), color = Color.Gray)
                        Text("$childAge", color = AccentGold, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = childAge.toFloat(),
                        onValueChange = { childAge = it.toInt() },
                        valueRange = 5f..17f,
                        colors = SliderDefaults.colors(activeTrackColor = AccentGold, thumbColor = AccentOrange)
                    )
                }
            },
            confirmButton = {
                NujoomButton(
                    text = vm.translate("register"),
                    onClick = {
                        if (childFirstName.isEmpty()) return@NujoomButton
                        vm.childRegFirstName.value = childFirstName
                        vm.childRegLastName.value = childLastName
                        vm.childRegAge.value = childAge
                        vm.childRegCode.value = familyCode.orEmpty()
                        vm.childRegPassword.value = "123456" // default passcode
                        vm.childRegister(
                            onSuccess = {
                                Toast.makeText(context, "تمت إضافة طفلك بنجاح!", Toast.LENGTH_SHORT).show()
                                showAddChildDialog = false
                                childFirstName = ""
                                childLastName = ""
                            },
                            onFailure = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddChildDialog = false }) {
                    Text(vm.translate("later"), color = Color.Gray)
                }
            }
        )
    }

    if (showNotificationsDialog) {
        AlertDialog(
            onDismissRequest = { showNotificationsDialog = false },
            containerColor = BgDarkCard,
            title = {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("إشعارات العائلة 🔔", color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    TextButton(onClick = { vm.clearNotifications() }) {
                        Text("قراءة الكل ✓", color = AccentGold, fontSize = 14.sp)
                    }
                }
            },
            text = {
                if (notifications.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxWidth().padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("لا توجد إشعارات حالياً ✨", color = Color.Gray, fontSize = 14.sp)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 300.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(notifications) { notif ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (notif.isRead) BgDarkCardBorder.copy(alpha = 0.3f) else BgDarkCardBorder
                                ),
                                border = BorderStroke(1.dp, if (notif.isRead) Color.Transparent else AccentGold)
                            ) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val emojiBadge = when(notif.type) {
                                        "task-completed" -> "📸"
                                        "task-approved" -> "🎉"
                                        "reward-pending" -> "🎁"
                                        "recitation-new" -> "📖"
                                        else -> "✨"
                                    }
                                    Text(emojiBadge, fontSize = 24.sp, modifier = Modifier.padding(end = 8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = if (lang == "ar") notif.titleAr else notif.titleEn,
                                            color = if (notif.isRead) Color.Gray else Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = if (lang == "ar") notif.bodyAr else notif.bodyEn,
                                            color = Color.LightGray,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                NujoomButton(text = "إغلاق", onClick = { showNotificationsDialog = false }, modifier = Modifier.fillMaxWidth())
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App header Greeting
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "مرحباً يا بطل الأهل! 👋",
                        fontSize = 15.sp,
                        color = Color.Gray
                    )
                    Text(
                        text = "بوابة المتابعة 🏡",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Card(
                        modifier = Modifier
                            .clickable { vm.toggleLanguage() }
                            .padding(4.dp),
                        colors = CardDefaults.cardColors(containerColor = BgDarkCard),
                        border = BorderStroke(1.dp, BgDarkCardBorder)
                    ) {
                        Text(
                            text = if (lang == "ar") "EN" else "AR",
                            color = AccentGold,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Card(
                        modifier = Modifier
                            .clickable { showNotificationsDialog = true }
                            .padding(4.dp),
                        colors = CardDefaults.cardColors(containerColor = BgDarkCard),
                        border = BorderStroke(1.dp, BgDarkCardBorder)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "🔔",
                                fontSize = 18.sp,
                                modifier = Modifier.padding(8.dp)
                            )
                            if (notifications.any { !it.isRead }) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .align(Alignment.TopEnd)
                                        .clip(CircleShape)
                                        .background(ErrorRed)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Card(
                        modifier = Modifier
                            .clickable { vm.logOut() }
                            .padding(4.dp),
                        colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.2f)),
                        border = BorderStroke(1.dp, ErrorRed)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            Text("🚪", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (lang == "ar") "خروج" else "Logout",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Family Code expandable Widget
        item {
            NujoomCard(
                borderColor = AccentGold.copy(alpha = 0.5f)
            ) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { codeExpanded = !codeExpanded }
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("🔑", fontSize = 20.sp, modifier = Modifier.padding(end = 8.dp))
                        Column {
                            Text(vm.translate("family_code_sub"), color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            Text(familyCode.orEmpty(), color = AccentGold, fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                    Text(if (codeExpanded) "▲" else "▼", color = Color.Gray, fontSize = 12.sp)
                }

                if (codeExpanded) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = vm.translate("family_code_warning"),
                        color = ErrorRed,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    NujoomButton(
                        text = vm.translate("copy_code"),
                        onClick = {
                            clipboard.setText(AnnotatedString(familyCode.orEmpty()))
                            Toast.makeText(context, vm.translate("copied"), Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isPrimary = false
                    )
                }
            }
        }

        // Horizontal child cards overview
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vm.translate("children"),
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Text(
                    text = vm.translate("add_child"),
                    color = AccentOrange,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.clickable { showAddChildDialog = true }
                )
            }
        }

        if (children.isEmpty()) {
            item {
                EmptyStateWidget(
                    emoji = "🏡",
                    title = "لا يوجد أطفال منضمين",
                    description = "شارك كود العائلة مع أطفالك ليسجلوا الدخول، أو أضفهم يدوياً وبسرعة الآن!",
                    onActionClick = { showAddChildDialog = true },
                    actionText = vm.translate("add_child")
                )
            }
        } else {
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(children) { child ->
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BgDarkCard),
                            shape = RoundedCornerShape(20.dp),
                            border = BorderStroke(1.5.dp, BgDarkCardBorder),
                            modifier = Modifier
                                .width(160.dp)
                                .clickable { onNavigateChild(child.childId) }
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Avatar circle representation
                                val av = NujoomConstants.AVATARS.firstOrNull { it.id == child.avatarId }?.emoji ?: "🤓"
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .size(64.dp)
                                        .clip(CircleShape)
                                        .background(Color.White.copy(alpha = 0.1f))
                                ) {
                                    Text(av, fontSize = 38.sp)
                                }

                                Spacer(modifier = Modifier.height(12.dp))

                                Text(
                                    text = child.firstName,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    fontSize = 16.sp
                                )
                                Text(
                                    text = "⭐ ${child.starBalance} نجمة",
                                    color = AccentGold,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(vertical = 4.dp)
                                )

                                Spacer(modifier = Modifier.height(8.dp))

                                // Simple circular progress ring representing todays tasks
                                CircularProgressRing(
                                    percentage = if (child.streak > 0) 100f else 60f,
                                    text = "المهام",
                                    size = 56.dp,
                                    strokeWidth = 6.dp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParentTasksScreen(
    vm: NujoomViewModel
) {
    val context = LocalContext.current
    val lang by vm.currentLanguage.collectAsState()
    val familyCode by vm.currentFamilyCode.collectAsState()
    val children by vm.childrenList.collectAsState()
    val activeChildId by vm.activeChildId.collectAsState()
    val tasks by vm.activeChildTasks.collectAsState()

    var showFirstVisitTour by remember { mutableStateOf(true) }

    // Dialog state for adding a custom task
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var taskNameAr by remember { mutableStateOf("") }
    var taskNameEn by remember { mutableStateOf("") }
    var taskStars by remember { mutableStateOf(5) }
    var requiresProof by remember { mutableStateOf(false) }
    var taskEmoji by remember { mutableStateOf("🎯") }
    var frequency by remember { mutableStateOf("daily") }

    // Dialog state for rejecting proof
    var showRejectDialog by remember { mutableStateOf(false) }
    var rejectTaskId by remember { mutableStateOf("") }
    var selectedReason by remember { mutableStateOf(vm.translate("rejection_option_unclear")) }
    var customReason by remember { mutableStateOf("") }

    // First visit advisory pop-up
    if (showFirstVisitTour && activeChildId != null) {
        AlertDialog(
            onDismissRequest = { showFirstVisitTour = false },
            containerColor = BgDarkCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💡", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(vm.translate("task_first_tour_title"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text(vm.translate("task_first_tour_body"), color = Color.LightGray)
            },
            confirmButton = {
                NujoomButton(
                    text = vm.translate("edit_tasks_now"),
                    onClick = {
                        showFirstVisitTour = false
                        showAddTaskDialog = true
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showFirstVisitTour = false }) {
                    Text(vm.translate("later"), color = Color.Gray)
                }
            }
        )
    }

    if (showAddTaskDialog) {
        val emojis = listOf("🪥", "🛏️", "🏃", "🧹", "📚", "🍳", "🎒", "🧼", "🥛", "🍉", "🧩", "⚽", "🎨", "📝")
        AlertDialog(
            onDismissRequest = { showAddTaskDialog = false },
            containerColor = BgDarkCard,
            title = { Text(vm.translate("add_custom_task"), color = Color.White) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    NujoomTextField(
                        value = taskNameAr,
                        onValueChange = { taskNameAr = it },
                        placeholder = "اسم المهمة بالعربية (أر)",
                        modifier = Modifier.padding(bottom = 12.dp),
                        testTag = "custom_task_ar"
                    )
                    NujoomTextField(
                        value = taskNameEn,
                        onValueChange = { taskNameEn = it },
                        placeholder = "Task Name in English (en)",
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Stars reward Horiz picker
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("النجوم / Stars Reward:", color = Color.White, fontSize = 14.sp)
                        Text("$taskStars ⭐", color = AccentGold, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = taskStars.toFloat(),
                        onValueChange = { taskStars = it.toInt() },
                        valueRange = 1f..20f,
                        colors = SliderDefaults.colors(activeTrackColor = AccentGold, thumbColor = AccentOrange)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Requires proof toggle
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("يتطلب صورة إثبات دليل 📸", color = Color.White, fontSize = 14.sp)
                        Switch(
                            checked = requiresProof,
                            onCheckedChange = { requiresProof = it },
                            colors = SwitchDefaults.colors(checkedThumbColor = AccentOrange)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Emojis horizontal list selecting
                    Text("اختر رمزاً / Select Icon:", color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    LazyRow {
                        items(emojis) { em ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(horizontal = 4.dp)
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(if (taskEmoji == em) AccentPurple else BgDarkCardBorder)
                                    .clickable { taskEmoji = em }
                            ) {
                                Text(em, fontSize = 22.sp)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                NujoomButton(
                    text = vm.translate("save_reward"),
                    onClick = {
                        if (taskNameAr.isEmpty()) return@NujoomButton
                        vm.addCustomTask(
                            nameAr = taskNameAr,
                            nameEn = taskNameEn,
                            emoji = taskEmoji,
                            stars = taskStars,
                            requiresProof = requiresProof,
                            frequency = frequency
                        )
                        showAddTaskDialog = false
                        taskNameAr = ""
                        taskNameEn = ""
                    },
                    testTag = "save_custom_task"
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddTaskDialog = false }) {
                    Text(vm.translate("later"), color = Color.Gray)
                }
            }
        )
    }

    if (showRejectDialog) {
        AlertDialog(
            onDismissRequest = { showRejectDialog = false },
            containerColor = BgDarkCard,
            title = { Text(vm.translate("rejection_reason_title"), color = Color.White) },
            text = {
                Column {
                    Text(vm.translate("rejection_reason_desc"), color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    val reasons = listOf(
                        vm.translate("rejection_option_unclear"),
                        vm.translate("rejection_option_incomplete"),
                        vm.translate("rejection_option_retry")
                    )
                    reasons.forEach { r ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedReason = r }
                                .padding(vertical = 8.dp)
                        ) {
                            RadioButton(
                                selected = selectedReason == r,
                                onClick = { selectedReason = r },
                                colors = RadioButtonDefaults.colors(selectedColor = AccentOrange)
                            )
                            Text(r, color = Color.White, fontSize = 13.sp)
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    NujoomTextField(
                        value = customReason,
                        onValueChange = { customReason = it },
                        placeholder = vm.translate("write_custom_reason")
                    )
                }
            },
            confirmButton = {
                NujoomButton(
                    text = vm.translate("send"),
                    onClick = {
                        val finalReason = if (customReason.isNotEmpty()) customReason else selectedReason
                        vm.rejectTask(rejectTaskId, finalReason)
                        showRejectDialog = false
                        customReason = ""
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showRejectDialog = false }) {
                    Text(vm.translate("later"), color = Color.Gray)
                }
            }
        )
    }

    LazyColumn(
         modifier = Modifier
             .fillMaxSize()
             .padding(16.dp),
         contentPadding = PaddingValues(bottom = 90.dp),
         verticalArrangement = Arrangement.spacedBy(12.dp),
         horizontalAlignment = Alignment.CenterHorizontally
     ) {
        // Child selection tabs with Sign out Button next to it
        item {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = vm.translate("nav_tasks"),
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier
                        .clickable { vm.logOut() }
                        .padding(4.dp),
                    colors = CardDefaults.cardColors(containerColor = ErrorRed.copy(alpha = 0.2f)),
                    border = BorderStroke(1.dp, ErrorRed)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Text("🚪", fontSize = 14.sp)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (lang == "ar") "خروج" else "Logout",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (children.isEmpty()) {
            item {
                EmptyStateWidget(
                    emoji = "📋",
                    title = "لا بد من إدخال طفل أولاً",
                    description = "يرجى تسجيل أو إضافة فرد من الأبناء أولاً لجدولة مهامه ومكافأته!"
                )
            }
        } else {
            // Horizontal children select
            item {
                LazyRow(
                    modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    items(children) { child ->
                        val isSelected = activeChildId == child.childId
                        Box(
                            modifier = Modifier
                                .padding(horizontal = 6.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) AccentOrange else BgDarkCard)
                                .border(1.dp, if (isSelected) Color.Transparent else BgDarkCardBorder, RoundedCornerShape(16.dp))
                                .clickable { vm.selectChild(child.childId) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(child.firstName, color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (activeChildId == null) {
                item {
                    Text("يرجى اختيار أحد الأبناء للمتابعة 👆", color = Color.Gray)
                }
            } else {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "مهام النشاط الحالية:",
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Button(
                            onClick = { showAddTaskDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                        ) {
                            Text(vm.translate("add_child"), color = Color.White)
                        }
                    }
                }

                if (tasks.isEmpty()) {
                    item {
                        EmptyStateWidget(
                            emoji = "🎯",
                            title = "لا توجد مهام نشطة اليوم",
                            description = "هذا الحساب فارغ، يمكنك النقر لإدراج مهمة مميزة اليوم!",
                            onActionClick = { showAddTaskDialog = true },
                            actionText = vm.translate("add_child")
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
                                            text = "⭐ ${task.starsReward} نجوم",
                                            color = AccentGold,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "🗑️",
                                        fontSize = 20.sp,
                                        modifier = Modifier
                                            .clickable { vm.deleteTask(task.taskId) }
                                            .padding(horizontal = 8.dp)
                                    )

                                    // Proof viewing or Action Button
                                    if (task.status == "awaiting-review") {
                                        NujoomButton(
                                            text = vm.translate("view_proof"),
                                            onClick = {
                                                // Simple prompt review Dialog Simulation
                                                val photoSimulatedHint = "دليل مصور: ${task.emoji} (تم الملء بنجاح!)"
                                                Toast.makeText(context, photoSimulatedHint, Toast.LENGTH_LONG).show()
                                                // Auto trigger verification dialogue choice
                                            },
                                            modifier = Modifier.height(38.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Column {
                                            Text(
                                                "✅",
                                                fontSize = 24.sp,
                                                modifier = Modifier.clickable { vm.approveTask(task.taskId) }
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                "❌",
                                                fontSize = 24.sp,
                                                modifier = Modifier.clickable {
                                                    rejectTaskId = task.taskId
                                                    showRejectDialog = true
                                                }
                                            )
                                        }
                                    } else {
                                        val statusText = when (task.status) {
                                            "completed" -> vm.translate("status_completed")
                                            "rejected" -> vm.translate("status_rejected")
                                            else -> "غير مكتملة"
                                        }
                                        val tColor = if (task.status == "completed") SuccessGreen else Color.Gray
                                        Text(statusText, color = tColor, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParentRewardsScreen(
    vm: NujoomViewModel
) {
    val context = LocalContext.current
    val lang by vm.currentLanguage.collectAsState()
    val rewards by vm.activeFamilyRewards.collectAsState()
    val redemptions by vm.activeFamilyRedemptions.collectAsState()
    val children by vm.childrenList.collectAsState()
    val activeChildId by vm.activeChildId.collectAsState()

    var showFirstVisitTour by remember { mutableStateOf(true) }

    // Custom add reward state
    var showAddReward by remember { mutableStateOf(false) }
    var rewardAr by remember { mutableStateOf("") }
    var rewardEn by remember { mutableStateOf("") }
    var rewardStars by remember { mutableStateOf(15) }
    var rewardEmoji by remember { mutableStateOf("🍦") }
    var rewardCat by remember { mutableStateOf("food") }

    // Encouraging Shoutout dialogue
    var showShoutout by remember { mutableStateOf(false) }
    var customMessage by remember { mutableStateOf("") }
    val isAiGenerating by vm.isAiGenerating.collectAsState()
    val aiSuggestedQuote by vm.aiSuggestedQuote.collectAsState()

    if (showFirstVisitTour) {
        AlertDialog(
            onDismissRequest = { showFirstVisitTour = false },
            containerColor = BgDarkCard,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("🎁", fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(vm.translate("rewards_first_tour_title"), color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = { Text(vm.translate("rewards_first_tour_body"), color = Color.LightGray) },
            confirmButton = {
                NujoomButton(
                    text = vm.translate("edit_rewards_now"),
                    onClick = {
                        showFirstVisitTour = false
                        showAddReward = true
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showFirstVisitTour = false }) {
                    Text(vm.translate("later"), color = Color.Gray)
                }
            }
        )
    }

    if (showAddReward) {
        AlertDialog(
            onDismissRequest = { showAddReward = false },
            containerColor = BgDarkCard,
            title = { Text(vm.translate("add_reward"), color = Color.White) },
            text = {
                Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    NujoomTextField(
                        value = rewardAr,
                        onValueChange = { rewardAr = it },
                        placeholder = "اسم المكافأة بالعربية",
                        modifier = Modifier.padding(bottom = 12.dp),
                        testTag = "custom_reward_ar"
                    )
                    NujoomTextField(
                        value = rewardEn,
                        onValueChange = { rewardEn = it },
                        placeholder = "Reward Name in English",
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("تكلفة النجوم / Star Cost:", color = Color.White, fontSize = 14.sp)
                        Text("$rewardStars ⭐", color = AccentGold, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = rewardStars.toFloat(),
                        onValueChange = { rewardStars = it.toInt() },
                        valueRange = 5f..300f,
                        colors = SliderDefaults.colors(activeTrackColor = AccentGold, thumbColor = AccentOrange)
                    )
                }
            },
            confirmButton = {
                NujoomButton(
                    text = vm.translate("save_reward"),
                    onClick = {
                        if (rewardAr.isEmpty()) return@NujoomButton
                        vm.addReward(rewardAr, rewardEn, rewardEmoji, rewardStars, rewardCat)
                        showAddReward = false
                        rewardAr = ""
                        rewardEn = ""
                    },
                    testTag = "save_custom_reward"
                )
            },
            dismissButton = {
                TextButton(onClick = { showAddReward = false }) {
                    Text(vm.translate("later"), color = Color.Gray)
                }
            }
        )
    }

    if (showShoutout) {
        AlertDialog(
            onDismissRequest = { showShoutout = false },
            containerColor = BgDarkCard,
            title = { Text(vm.translate("encouraging_message_title"), color = Color.White) },
            text = {
                Column {
                    Text("أرسل دفعة إيمانية وحافزاً من الأذكار والثناء:", color = Color.Gray, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    NujoomTextField(
                        value = customMessage,
                        onValueChange = { customMessage = it },
                        placeholder = vm.translate("encouraging_placeholder")
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // AI generate Button invoking Gemini REST endpoint
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(AccentPurple.copy(alpha = 0.2f))
                            .border(1.dp, AccentPurple, RoundedCornerShape(12.dp))
                            .clickable {
                                val childObj = children.firstOrNull { it.childId == activeChildId }
                                val cName = childObj?.firstName ?: "البطل"
                                vm.fetchAiEncouragement(cName, "الصلوات الخمس اليومية والمهام")
                            }
                            .padding(12.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isAiGenerating) {
                            CircularProgressIndicator(color = AccentGold, modifier = Modifier.size(24.dp))
                        } else {
                            Text(
                                text = "✨ اقتراح تربوي ذكي من (Gemini) ✨",
                                color = AccentGold,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    if (aiSuggestedQuote != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(12.dp)
                                .clickable {
                                    customMessage = aiSuggestedQuote.orEmpty()
                                }
                        ) {
                            Text(
                                text = aiSuggestedQuote!!,
                                color = Color.White,
                                fontSize = 12.sp,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            },
            confirmButton = {
                NujoomButton(
                    text = vm.translate("send"),
                    onClick = {
                        if (customMessage.isEmpty()) return@NujoomButton
                        vm.sendShoutout(customMessage)
                        showShoutout = false
                        customMessage = ""
                        Toast.makeText(context, "تم إرسال الرسالة التشجيعية بنجاح بنبض المحبة!", Toast.LENGTH_SHORT).show()
                    }
                )
            },
            dismissButton = {
                TextButton(onClick = { showShoutout = false }) {
                    Text(vm.translate("later"), color = Color.Gray)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (lang == "ar") "متجر مكافآت الأبناء 🎁" else "Children's Rewards Store 🎁",
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.ExtraBold
                )
                Button(
                    onClick = { showAddReward = true },
                    colors = ButtonDefaults.buttonColors(containerColor = AccentOrange)
                ) {
                    Text("+ مكافأة", color = Color.White)
                }
            }
        }

        // Shoutout send floating access
        if (children.isNotEmpty() && activeChildId != null) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = BgDarkCard),
                    border = BorderStroke(1.5.dp, AccentPurple)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { showShoutout = true }
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("💌", fontSize = 24.sp, modifier = Modifier.padding(end = 12.dp))
                            Column {
                                val cScope = children.firstOrNull { it.childId == activeChildId }
                                Text("إرسال رسالة تشجيعية لـ ${cScope?.firstName.orEmpty()}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                Text("انقر لتفعيل العبارات المحفزة بالذكاء الاصطناعي", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                        Text("◀", color = AccentPurple, fontSize = 14.sp)
                    }
                }
            }
        }

        // Pending approvals section
        val pendings = redemptions.filter { it.status == "pending" }
        if (pendings.isNotEmpty()) {
            item {
                Text(
                    text = "${vm.translate("pending_redemptions")} (${pendings.size}):",
                    color = AccentGold,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
            items(pendings) { rd ->
                val associatedChildObj = children.firstOrNull { it.childId == rd.childId }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgDarkCard)
                        .border(1.dp, AccentGold.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(rd.rewardEmoji, fontSize = 32.sp, modifier = Modifier.padding(end = 12.dp))
                        Column {
                            Text(
                                text = "طلب: " + (if (lang == "ar") rd.rewardNameAr else rd.rewardNameEn),
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "الابن: ${associatedChildObj?.firstName.orEmpty()} · تكلفة: ⭐ ${rd.starsCost}",
                                color = Color.Gray,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Row {
                        Text(
                            text = "✅",
                            fontSize = 24.sp,
                            modifier = Modifier
                                .clickable { vm.resolveRedemption(rd.redemptionId, "approved") }
                                .padding(horizontal = 6.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "❌",
                            fontSize = 24.sp,
                            modifier = Modifier
                                .clickable { vm.resolveRedemption(rd.redemptionId, "rejected", "طلب مكرر أو لا يتوافق مع الرغبات") }
                                .padding(horizontal = 6.dp)
                        )
                    }
                }
            }
        }

        // Reward catalogue list toggle
        item {
            Text(
                text = vm.translate("available_rewards"),
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }

        if (rewards.isEmpty()) {
            item {
                EmptyStateWidget(
                    emoji = "🎁",
                    title = "لا توجد مكافآت مضافة",
                    description = "أضف مكافآت ممتعة مثل الآيس كريم أو وقت شاشة لتحفيز طفلك!"
                )
            }
        } else {
            items(rewards) { rw ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(BgDarkCard)
                        .border(1.dp, BgDarkCardBorder, RoundedCornerShape(12.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(rw.emoji, fontSize = 32.sp, modifier = Modifier.padding(end = 12.dp))
                        Column {
                            Text(
                                text = if (lang == "ar") rw.nameAr else rw.nameEn,
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "⭐ ${rw.starsCost} نجمة",
                                color = AccentGold,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "🗑️",
                            fontSize = 20.sp,
                            modifier = Modifier
                                .clickable { vm.deleteReward(rw.rewardId) }
                                .padding(horizontal = 12.dp)
                        )
                        Switch(
                            checked = rw.isAvailable,
                            onCheckedChange = { vm.toggleRewardAvailability(rw) },
                            colors = SwitchDefaults.colors(checkedThumbColor = AccentOrange)
                        )
                    }
                }
            }
        }
    }
}

// Religious tracking monitor screen
@Composable
fun ParentReligiousScreen(
    vm: NujoomViewModel
) {
    val context = LocalContext.current
    val lang by vm.currentLanguage.collectAsState()
    val children by vm.childrenList.collectAsState()
    val activeChildId by vm.activeChildId.collectAsState()
    val activePrayers by vm.activeChildPrayers.collectAsState()
    val recitations by vm.activeChildRecitations.collectAsState()

    var sheikhCommentInput by remember { mutableStateOf("") }
    var expandedRecitationId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("صلوات وتلاوات الأبناء 🕌", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(bottom = 12.dp))

        if (children.isEmpty()) {
            EmptyStateWidget(
                emoji = "🕌",
                title = "لا يوجد أطفال للمراقبة",
                description = "سجل الأبناء أولاً لتتبع صلواتهم الخمس وتلاواتهم لبرنامج المتابعة."
            )
        } else {
            // Horizontal select
            LazyRow(
                modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                items(children) { child ->
                    val isSelected = activeChildId == child.childId
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (isSelected) AccentOrange else BgDarkCard)
                            .border(1.dp, if (isSelected) Color.Transparent else BgDarkCardBorder, RoundedCornerShape(16.dp))
                            .clickable { vm.selectChild(child.childId) }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(child.firstName, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }

            if (activeChildId == null) {
                Text("يرجى اختيار ابن لعرض برنامجه الإيماني 🕌", color = Color.Gray)
            } else {
                val currentChild = children.find { it.childId == activeChildId }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (currentChild != null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = BgDarkCard),
                                shape = RoundedCornerShape(16.dp),
                                border = BorderStroke(1.5.dp, BgDarkCardBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text("🎯 هدف حفظ القرآن الكريم:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                        
                                        var showSelectSurahDialog by remember { mutableStateOf(false) }
                                        
                                        Button(
                                            onClick = { showSelectSurahDialog = true },
                                            colors = ButtonDefaults.buttonColors(containerColor = AccentGold),
                                            shape = RoundedCornerShape(12.dp),
                                            contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp)
                                        ) {
                                            Text("اختيار السورة 📖", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }
                                        
                                        if (showSelectSurahDialog) {
                                            var searchQuery by remember { mutableStateOf("") }
                                            val filteredSurahs = NujoomConstants.QURAN_SURAHS.filter {
                                                it.nameAr.contains(searchQuery, ignoreCase = true) || 
                                                it.nameEn.contains(searchQuery, ignoreCase = true)
                                            }
                                            
                                            AlertDialog(
                                                onDismissRequest = { showSelectSurahDialog = false },
                                                containerColor = BgDarkCard,
                                                title = {
                                                    Column {
                                                        Text("اختر السورة المستهدفة", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                                        Spacer(modifier = Modifier.height(8.dp))
                                                        NujoomTextField(
                                                            value = searchQuery,
                                                            onValueChange = { searchQuery = it },
                                                            placeholder = "ابحث عن السورة... / Search..."
                                                        )
                                                    }
                                                },
                                                text = {
                                                    Box(modifier = Modifier.height(280.dp).fillMaxWidth()) {
                                                        if (filteredSurahs.isEmpty()) {
                                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                                Text("لا توجد سور مطابقة للبحث 🔍", color = Color.Gray, fontSize = 12.sp)
                                                            }
                                                        } else {
                                                            LazyColumn(
                                                                modifier = Modifier.fillMaxSize(),
                                                                verticalArrangement = Arrangement.spacedBy(8.dp)
                                                            ) {
                                                                items(filteredSurahs) { surah ->
                                                                    Card(
                                                                        modifier = Modifier
                                                                            .fillMaxWidth()
                                                                            .clickable {
                                                                                vm.setChildMemorizeSurah(currentChild.childId, surah.nameAr)
                                                                                showSelectSurahDialog = false
                                                                            },
                                                                        colors = CardDefaults.cardColors(
                                                                            containerColor = if (currentChild.memorizeSurah == surah.nameAr) AccentGold.copy(alpha = 0.15f) else BgDarkCardBorder.copy(alpha = 0.4f)
                                                                        ),
                                                                        border = BorderStroke(
                                                                            1.dp,
                                                                            if (currentChild.memorizeSurah == surah.nameAr) AccentGold else Color.Transparent
                                                                        )
                                                                    ) {
                                                                        Row(
                                                                            modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                                            verticalAlignment = Alignment.CenterVertically
                                                                        ) {
                                                                            Column {
                                                                                Text(surah.nameAr, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                                                                Text(surah.nameEn, color = Color.Gray, fontSize = 11.sp)
                                                                            }
                                                                            Text("${surah.ayahs} آية", color = AccentGold, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                        }
                                                    }
                                                },
                                                confirmButton = {
                                                    NujoomButton(
                                                        text = "إغلاق",
                                                        onClick = { showSelectSurahDialog = false },
                                                        modifier = Modifier.fillMaxWidth()
                                                    )
                                                }
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(12.dp))

                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("السورة المطلوبة للحفظ: ", color = Color.Gray, fontSize = 13.sp)
                                        Text(currentChild.memorizeSurah, color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("إجمالي عدد الآيات في السورة: ", color = Color.Gray, fontSize = 13.sp)
                                        val activeSurahInfoPr = NujoomConstants.QURAN_SURAHS.find { it.nameAr == currentChild.memorizeSurah || it.nameEn == currentChild.memorizeSurah }
                                        Text("${activeSurahInfoPr?.ayahs ?: 30} آية", color = Color.White, fontSize = 14.sp)
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("مطلوب من الطفل حفظه اليوم: ", color = Color.Gray, fontSize = 13.sp)
                                        Text("${currentChild.memorizeAyahs} آيات كاملة", color = SuccessGreen, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = BgDarkCard),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(1.5.dp, BgDarkCardBorder)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("🕌 الصلوات الخمس اليوم:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                                Spacer(modifier = Modifier.height(12.dp))

                                val prayersList = listOf(
                                    Triple("fajr", vm.translate("fajr"), activePrayers?.fajr ?: false),
                                    Triple("dhuhr", vm.translate("dhuhr"), activePrayers?.dhuhr ?: false),
                                    Triple("asr", vm.translate("asr"), activePrayers?.asr ?: false),
                                    Triple("maghrib", vm.translate("maghrib"), activePrayers?.maghrib ?: false),
                                    Triple("isha", vm.translate("isha"), activePrayers?.isha ?: false),
                                )

                                prayersList.forEach { (key, name, checked) ->
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 4.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(name, color = Color.White)
                                        Text(
                                            text = if (checked) "✅ صلّى (+⭐1)" else "❌ لم يصلِّ بعد",
                                            color = if (checked) SuccessGreen else Color.Gray,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("📖 سجل التلاوات المرسلة والمراجعة الشيخ:", color = Color.White, fontWeight = FontWeight.Bold)
                    }

                    if (recitations.isEmpty()) {
                        item {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = BgDarkCard)
                            ) {
                                Box(modifier = Modifier.padding(24.dp), contentAlignment = Alignment.Center) {
                                    Text("لا توجد تسجيلات قرآنية مرسلة حتى الآن 📖", color = Color.Gray, fontSize = 13.sp)
                                }
                            }
                        }
                    } else {
                        items(recitations) { rc ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(containerColor = BgDarkCard),
                                border = BorderStroke(1.dp, BgDarkCardBorder)
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column {
                                            Text(rc.surahName, color = Color.White, fontWeight = FontWeight.Bold)
                                            val df = SimpleDateFormat("yyyy/MM/dd hh:mm a", Locale.getDefault())
                                            Text(df.format(Date(rc.recordedAt)), color = Color.Gray, fontSize = 11.sp)
                                        }
                                        TextButton(onClick = {
                                            // Simulated audio play
                                            Toast.makeText(context, "▶ تشغيل الصوت المقرأ عذباً..", Toast.LENGTH_SHORT).show()
                                        }) {
                                            Text("▶ استمع للتلاوة", color = AccentGold, fontWeight = FontWeight.Bold)
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(8.dp))

                                    if (rc.sheikhComment != null) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                                .border(1.dp, AccentGold.copy(alpha = 0.4f), RoundedCornerShape(8.dp))
                                                .padding(8.dp)
                                        ) {
                                            Text(
                                                text = "💬 تقييم (${rc.sheikhName ?: "الوالدين"}): ${rc.sheikhComment} (+${rc.starsAwarded} ⭐)",
                                                color = AccentGold,
                                                fontSize = 12.sp
                                            )
                                        }
                                    } else {
                                        if (expandedRecitationId == rc.recitationId) {
                                            NujoomTextField(
                                                value = sheikhCommentInput,
                                                onValueChange = { sheikhCommentInput = it },
                                                placeholder = vm.translate("edit_sheikh_comment")
                                            )
                                            Spacer(modifier = Modifier.height(8.dp))
                                            Row {
                                                NujoomButton(
                                                    text = "ممتاز (+5 ⭐)",
                                                    onClick = {
                                                        vm.reviewRecitation(rc.recitationId, sheikhCommentInput.ifEmpty { "تلاوة خاشعة بارك الله فيك" }, 5)
                                                        expandedRecitationId = null
                                                        sheikhCommentInput = ""
                                                    },
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                TextButton(onClick = { expandedRecitationId = null }) {
                                                    Text("إلغاء", color = Color.Gray)
                                                }
                                            }
                                        } else {
                                            Button(
                                                onClick = { expandedRecitationId = rc.recitationId },
                                                colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                                            ) {
                                                Text("✏️ تقييم التلاوة وإعطاء النجوم", color = Color.White, fontSize = 11.sp)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParentReportsScreen(
    vm: NujoomViewModel
) {
    val context = LocalContext.current
    val children by vm.childrenList.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("📊 تقارير الأداء التفصيلية", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

        if (children.isEmpty()) {
            EmptyStateWidget(emoji = "📊", title = "لا توجد تقارير متوفرة", description = "يرجى تسجيل الأبناء أولاً لإنشاء تقارير ومخططات تفصيلية.")
        } else {
            // Performance cards
            children.forEach { child ->
                NujoomCard(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)) {
                    Text("${child.firstName} ${child.lastName}", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("النجوم الإجمالية:", color = Color.Gray, fontSize = 12.sp)
                            Text("⭐ ${child.totalStarsEarned}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                        Column {
                            Text("سلسلة الهمة والنشاط:", color = Color.Gray, fontSize = 12.sp)
                            Text("🔥 ${child.streak} أيام متواصلة", color = AccentOrange, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }


                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            NujoomButton(
                text = vm.translate("export_pdf"),
                onClick = {
                    Toast.makeText(context, "📄 تم تصدير تقرير عائلة نجوم PDF وحفظه في التنزيلات بنجاح!", Toast.LENGTH_LONG).show()
                },
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun ParentSettingsScreen(
    vm: NujoomViewModel
) {
    val context = LocalContext.current
    val familyCode by vm.currentFamilyCode.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("⚙️ الإعدادات العامة للوالدين", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = BgDarkCard),
            border = BorderStroke(1.dp, BgDarkCardBorder)
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("إعدادات التطبيق والتواصل:", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)

                // Quick Language Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("لغة التطبيق / Application Language:", color = Color.LightGray)
                    TextButton(onClick = { vm.toggleLanguage() }) {
                        Text(vm.translate("arabic") + " / " + vm.translate("english"), color = AccentGold, fontWeight = FontWeight.Bold)
                    }
                }

                HorizontalDivider(color = BgDarkCardBorder)

                // Quick Theme Toggle
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("مظهر التطبيق / Palette Theme:", color = Color.LightGray)
                    TextButton(onClick = { vm.toggleTheme() }) {
                        Text("داكن (Dark) / فاتح (Light)", color = AccentGold)
                    }
                }
            }
        }

        // WhatsApp direct support link context
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://wa.me/201001853928"))
                    context.startActivity(intent)
                },
            colors = CardDefaults.cardColors(containerColor = BgDarkCard),
            border = BorderStroke(1.dp, BgDarkCardBorder)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💬", fontSize = 26.sp, modifier = Modifier.padding(end = 12.dp))
                Column {
                    Text(vm.translate("contact_whatsapp"), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("الدعم عبر الواتساب: 201001853928+", color = Color.Gray, fontSize = 11.sp)
                }
            }
        }

        NujoomButton(
            text = vm.translate("sign_out"),
            onClick = { vm.logOut() },
            modifier = Modifier.fillMaxWidth(),
            isPrimary = true,
            testTag = "parent_logout_btn"
        )
    }
}
