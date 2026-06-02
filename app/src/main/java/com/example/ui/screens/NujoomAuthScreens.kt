package com.example.ui.screens

import android.widget.Toast
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.*
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.NujoomViewModel

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
fun SplashScreen(
    vm: NujoomViewModel,
    onNavigateNext: () -> Unit
) {
    NujoomGradientBg {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⭐",
                fontSize = 80.sp,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            Text(
                text = "نجوم",
                fontSize = 42.sp,
                color = AccentGold,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )
            Text(
                text = "مهام أطفالك · مكافآت حقيقية",
                fontSize = 17.sp,
                color = Color.LightGray,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp)
            )

            Spacer(modifier = Modifier.height(64.dp))

            NujoomButton(
                text = "ابدأ وبناء رحلتي 💫",
                onClick = onNavigateNext,
                modifier = Modifier.width(200.dp)
            )
        }
    }
}

@Composable
fun WelcomeScreen(
    vm: NujoomViewModel,
    onNavigateParentLogin: () -> Unit,
    onNavigateChildLogin: () -> Unit
) {
    val lang by vm.currentLanguage.collectAsState()

    NujoomGradientBg {
        // Language Toggle App Bar on top-right
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = if (lang == "ar") Arrangement.End else Arrangement.Start
        ) {
            Button(
                onClick = { vm.toggleLanguage() },
                colors = ButtonDefaults.buttonColors(containerColor = BgDarkCard),
                border = BorderStroke(1.dp, BgDarkCardBorder),
                shape = RoundedCornerShape(20.dp)
            ) {
                Text(
                    text = if (lang == "ar") "English" else "العربية",
                    color = AccentGold,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "⭐",
                fontSize = 90.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            Text(
                text = vm.translate("app_name"),
                fontSize = 46.sp,
                color = AccentGold,
                fontWeight = FontWeight.ExtraBold
            )
            Text(
                text = vm.translate("app_tagline"),
                fontSize = 15.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 48.dp)
            )

            NujoomButton(
                text = vm.translate("welcome_parent"),
                onClick = onNavigateParentLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                isPrimary = true,
                testTag = "welcome_parent_btn"
            )

            NujoomButton(
                text = vm.translate("welcome_child"),
                onClick = onNavigateChildLogin,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                isPrimary = false,
                testTag = "welcome_child_btn"
            )
        }
    }
}

@Composable
fun ParentLoginScreen(
    vm: NujoomViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateRegister: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var email by vm.parentLoginEmail
    var password by vm.parentLoginPassword

    NujoomGradientBg {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "🔙",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .clickable { onBack() }
                        .padding(12.dp)
                )
            }

            Text("⭐", fontSize = 48.sp, modifier = Modifier.padding(bottom = 12.dp))
            Text(
                vm.translate("welcome_parent"),
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                vm.translate("login"),
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            NujoomTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = vm.translate("email"),
                modifier = Modifier.padding(bottom = 16.dp),
                testTag = "parent_email_input"
            )

            NujoomTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = vm.translate("password"),
                modifier = Modifier.padding(bottom = 16.dp),
                isPassword = true,
                testTag = "parent_password_input"
            )

            var rememberMe by vm.parentRememberMe
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clickable { rememberMe = !rememberMe }
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AccentOrange,
                        uncheckedColor = Color.Gray,
                        checkmarkColor = Color.White
                    )
                )
                Text(
                    text = vm.translate("remember_me"),
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            NujoomButton(
                text = vm.translate("login"),
                onClick = {
                    vm.parentLogin(
                        onSuccess = {
                            Toast.makeText(context, "أهلاً بك يا بطل! تم الدخول", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        },
                        onFailure = { err ->
                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "parent_submit_login"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = vm.translate("no_account"),
                color = AccentGold,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onNavigateRegister() }
                    .padding(12.dp)
            )
        }
    }
}

@Composable
fun ParentRegisterScreen(
    vm: NujoomViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateLogin: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var name by vm.parentRegName
    var email by vm.parentRegEmail
    var password by vm.parentRegPassword
    var phone by vm.parentPhone
    var agreed by vm.termsAgreed

    // Dialog state for successfully generated family code representation
    var showCodeDialog by remember { mutableStateOf(false) }
    val generatedCode by vm.generatedFamilyCodeToShow

    if (showCodeDialog && generatedCode != null) {
        val clipboard = LocalClipboardManager.current
        AlertDialog(
            onDismissRequest = { },
            containerColor = BgDarkCard,
            tonalElevation = 8.dp,
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text("⭐", fontSize = 38.sp)
                    Text(
                        vm.translate("family_code_success"),
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(vm.translate("family_code_sub"), color = Color.Gray, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Box(
                        modifier = Modifier
                            .background(Color.Black.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            .border(1.5.dp, AccentGold, RoundedCornerShape(12.dp))
                            .padding(vertical = 12.dp, horizontal = 24.dp)
                    ) {
                        Text(
                            text = generatedCode!!,
                            color = AccentGold,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold,
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        vm.translate("family_code_warning"),
                        color = ErrorRed,
                        fontSize = 11.sp,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                    NujoomButton(
                        text = vm.translate("copy_code"),
                        onClick = {
                            clipboard.setText(AnnotatedString(generatedCode!!))
                            Toast.makeText(context, vm.translate("copied"), Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        isPrimary = false
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    NujoomButton(
                        text = vm.translate("continue_to_home"),
                        onClick = {
                            showCodeDialog = false
                            onRegisterSuccess()
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        )
    }

    NujoomGradientBg {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "🔙",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .clickable { onBack() }
                        .padding(12.dp)
                )
            }

            Text("⭐", fontSize = 48.sp, modifier = Modifier.padding(bottom = 12.dp))
            Text(
                vm.translate("register"),
                fontSize = 26.sp,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            NujoomTextField(
                value = name,
                onValueChange = { name = it },
                placeholder = vm.translate("full_name"),
                modifier = Modifier.padding(bottom = 12.dp),
                testTag = "parent_reg_name"
            )

            NujoomTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = vm.translate("email"),
                modifier = Modifier.padding(bottom = 12.dp),
                testTag = "parent_reg_email"
            )

            NujoomTextField(
                value = phone,
                onValueChange = { phone = it },
                placeholder = vm.translate("phone"),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            NujoomTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = vm.translate("password"),
                modifier = Modifier.padding(bottom = 16.dp),
                isPassword = true,
                testTag = "parent_reg_pass"
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clickable { agreed = !agreed }
            ) {
                Checkbox(
                    checked = agreed,
                    onCheckedChange = { agreed = it },
                    colors = CheckboxDefaults.colors(checkedColor = AccentOrange)
                )
                Text(vm.translate("terms"), color = Color.White, fontSize = 12.sp)
            }

            NujoomButton(
                text = vm.translate("register"),
                onClick = {
                    vm.parentRegister(
                        onSuccess = {
                            showCodeDialog = true
                        },
                        onFailure = { err ->
                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "parent_submit_reg"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = vm.translate("already_account"),
                color = AccentGold,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onNavigateLogin() }
                    .padding(12.dp)
            )
        }
    }
}

// Child Register Flow containing Avatar & colored frame selectors
@Composable
fun ChildRegisterScreen(
    vm: NujoomViewModel,
    onRegisterSuccess: () -> Unit,
    onNavigateLogin: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var firstName by vm.childRegFirstName
    var lastName by vm.childRegLastName
    var code by vm.childRegCode
    var age by vm.childRegAge
    var pass by vm.childRegPassword

    var isAvatarStep by remember { mutableStateOf(false) }

    if (isAvatarStep) {
        AvatarSelectScreen(
            vm = vm,
            onSubmitComplete = {
                vm.childRegister(
                    onSuccess = {
                        Toast.makeText(context, "أهلاً بك يا بطل! تم الانضمام بنجاح 🎉", Toast.LENGTH_SHORT).show()
                        onRegisterSuccess()
                    },
                    onFailure = { err ->
                        Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                        isAvatarStep = false
                    }
                )
            },
            onBack = { isAvatarStep = false }
        )
    } else {
        NujoomGradientBg {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Text(
                        text = "🔙",
                        fontSize = 24.sp,
                        modifier = Modifier
                            .clickable { onBack() }
                            .padding(12.dp)
                    )
                }

                Text("🎮", fontSize = 48.sp, modifier = Modifier.padding(bottom = 12.dp))
                Text(
                    vm.translate("welcome_child"),
                    fontSize = 26.sp,
                    color = Color.White,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.padding(bottom = 6.dp)
                )

                NujoomTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    placeholder = vm.translate("first_name"),
                    modifier = Modifier.padding(bottom = 12.dp),
                    testTag = "child_reg_firstname"
                )

                NujoomTextField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    placeholder = vm.translate("last_name"),
                    modifier = Modifier.padding(bottom = 12.dp),
                    testTag = "child_reg_lastname"
                )

                NujoomTextField(
                    value = code,
                    onValueChange = { code = it },
                    placeholder = vm.translate("family_code"),
                    modifier = Modifier.padding(bottom = 12.dp),
                    testTag = "child_reg_code"
                )

                NujoomTextField(
                    value = pass,
                    onValueChange = { pass = it },
                    placeholder = vm.translate("password"),
                    modifier = Modifier.padding(bottom = 16.dp),
                    isPassword = true
                )

                // Age slider Picker represented as clean compose rows
                Column(modifier = Modifier.fillMaxWidth().padding(bottom = 24.dp)) {
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(vm.translate("age"), color = Color.Gray, fontSize = 14.sp)
                        Text("$age", color = AccentGold, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                    Slider(
                        value = age.toFloat(),
                        onValueChange = { age = it.toInt() },
                        valueRange = 5f..17f,
                        colors = SliderDefaults.colors(
                            activeTrackColor = AccentGold,
                            thumbColor = AccentOrange
                        )
                    )
                }

                NujoomButton(
                    text = vm.translate("select_this_avatar"),
                    onClick = {
                        if (firstName.length < 2) {
                            Toast.makeText(context, "الاسم الأول قصير جداً / Name too short", Toast.LENGTH_SHORT).show()
                            return@NujoomButton
                        }
                        if (code.isEmpty()) {
                            Toast.makeText(context, "كود العائلة مطلوب / Missing Code", Toast.LENGTH_SHORT).show()
                            return@NujoomButton
                        }
                        // Advance to avatar selection step
                        isAvatarStep = true
                    },
                    modifier = Modifier.fillMaxWidth(),
                    testTag = "child_proceed_avatar"
                )

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = vm.translate("already_account"),
                    color = AccentGold,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier
                        .clickable { onNavigateLogin() }
                        .padding(12.dp)
                )
            }
        }
    }
}

@Composable
fun AvatarSelectScreen(
    vm: NujoomViewModel,
    onSubmitComplete: () -> Unit,
    onBack: () -> Unit
) {
    var selectedAv by vm.selectedAvatarId
    var selectedFr by vm.selectedAvatarFrame

    NujoomGradientBg {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "🔙",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .clickable { onBack() }
                        .padding(8.dp)
                )
            }

            Text(
                vm.translate("choose_avatar"),
                fontSize = 22.sp,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            // Current Preview in colored selected border frame
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(100.dp)
                    .clip(CircleShape)
                    .background(parseHexColor(selectedFr).copy(alpha = 0.2f))
                    .border(3.dp, parseHexColor(selectedFr), CircleShape)
            ) {
                val foundEmoji = NujoomConstants.AVATARS.firstOrNull { it.id == selectedAv }?.emoji ?: "🤓"
                Text(foundEmoji, fontSize = 52.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // colored frame list
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
            ) {
                NujoomConstants.FRAMES.forEach { (colorStr, _) ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 6.dp)
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(parseHexColor(colorStr))
                            .border(
                                width = if (selectedFr == colorStr) 3.dp else 0.dp,
                                color = Color.White,
                                shape = CircleShape
                            )
                            .clickable { selectedFr = colorStr }
                    )
                }
            }

            // Grid of Avatars
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                items(NujoomConstants.AVATARS) { av ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .padding(8.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (selectedAv == av.id) BgDarkCardBorder else BgDarkCard)
                            .border(
                                1.5.dp,
                                if (selectedAv == av.id) AccentGold else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { selectedAv = av.id }
                    ) {
                        Text(av.emoji, fontSize = 42.sp)
                    }
                }
            }

            NujoomButton(
                text = vm.translate("register"),
                onClick = onSubmitComplete,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                testTag = "child_finish_reg"
            )
        }
    }
}

@Composable
fun ChildLoginScreen(
    vm: NujoomViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateRegister: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var firstName by remember { mutableStateOf(vm.repository.getChildRememberFirstName()) }
    var lastName by remember { mutableStateOf(vm.repository.getChildRememberLastName()) }
    var code by remember { mutableStateOf(vm.repository.getChildRememberCode()) }
    var rememberMe by remember { mutableStateOf(vm.repository.isChildRememberEnabled()) }

    NujoomGradientBg {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Start
            ) {
                Text(
                    text = "🔙",
                    fontSize = 24.sp,
                    modifier = Modifier
                        .clickable { onBack() }
                        .padding(12.dp)
                )
            }

            Text("🎮", fontSize = 48.sp, modifier = Modifier.padding(bottom = 12.dp))
            Text(
                vm.translate("welcome_child"),
                fontSize = 28.sp,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                modifier = Modifier.padding(bottom = 6.dp)
            )
            Text(
                vm.translate("login"),
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            NujoomTextField(
                value = firstName,
                onValueChange = { firstName = it },
                placeholder = vm.translate("first_name"),
                modifier = Modifier.padding(bottom = 12.dp),
                testTag = "child_login_firstname"
            )

            NujoomTextField(
                value = lastName,
                onValueChange = { lastName = it },
                placeholder = vm.translate("last_name"),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            NujoomTextField(
                value = code,
                onValueChange = { code = it },
                placeholder = vm.translate("family_code"),
                modifier = Modifier.padding(bottom = 16.dp),
                testTag = "child_login_code"
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp)
                    .clickable { rememberMe = !rememberMe }
            ) {
                Checkbox(
                    checked = rememberMe,
                    onCheckedChange = { rememberMe = it },
                    colors = CheckboxDefaults.colors(
                        checkedColor = AccentOrange,
                        uncheckedColor = Color.Gray,
                        checkmarkColor = Color.White
                    )
                )
                Text(
                    text = vm.translate("remember_me"),
                    color = Color.White,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }

            NujoomButton(
                text = vm.translate("login"),
                onClick = {
                    if (firstName.isEmpty() || code.isEmpty()) {
                        Toast.makeText(context, "يرجى ملء جميع الحقول / Fill all fields", Toast.LENGTH_SHORT).show()
                        return@NujoomButton
                    }
                    vm.childLogin(
                        familyCode = code,
                        firstName = firstName,
                        lastName = lastName,
                        remember = rememberMe,
                        onSuccess = {
                            Toast.makeText(context, "أهلاً ومرحباً بك يا بطل! 🌟", Toast.LENGTH_SHORT).show()
                            onLoginSuccess()
                        },
                        onFailure = { err ->
                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                        }
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                testTag = "child_submit_login"
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = vm.translate("no_account"),
                color = AccentGold,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onNavigateRegister() }
                    .padding(12.dp)
            )
        }
    }
}
