package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.*
import com.example.ui.theme.*
import com.example.viewmodel.NujoomViewModel

sealed class NujoomScreen {
    object Splash : NujoomScreen()
    object Welcome : NujoomScreen()
    object ParentLogin : NujoomScreen()
    object ParentRegister : NujoomScreen()
    object ChildLogin : NujoomScreen()
    object ChildRegister : NujoomScreen()
    object ParentDashboard : NujoomScreen()
    object ChildDashboard : NujoomScreen()
}

enum class ParentTab {
    Home, Tasks, Rewards, Religious
}

enum class ChildTab {
    Home, Prayers, Quran, Rewards
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm = remember { NujoomViewModel(application) }
            val currentLanguage by vm.currentLanguage.collectAsState()
            val themeMode by vm.currentTheme.collectAsState()

            var currentScreen by remember { mutableStateOf<NujoomScreen>(NujoomScreen.Splash) }
            var activeParentTab by remember { mutableStateOf(ParentTab.Home) }
            var activeChildTab by remember { mutableStateOf(ChildTab.Home) }

            // Automatic redirection if user already has a session
            LaunchedEffect(Unit) {
                // Keep brief splash screen representation, check session on startup
                vm.checkInitialSession(
                    onSuccessParent = {
                        currentScreen = NujoomScreen.ParentDashboard
                    },
                    onSuccessChild = {
                        currentScreen = NujoomScreen.ChildDashboard
                    },
                    onNoSession = {
                        // Stay on splash, wait for user to click
                    }
                )
            }

            NujoomTheme(darkTheme = themeMode == "dark") {
                Scaffold { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screen_transition"
                        ) { screen ->
                            when (screen) {
                                is NujoomScreen.Splash -> {
                                    SplashScreen(vm = vm, onNavigateNext = {
                                        currentScreen = NujoomScreen.Welcome
                                    })
                                }
                                is NujoomScreen.Welcome -> {
                                    WelcomeScreen(
                                        vm = vm,
                                        onNavigateParentLogin = { currentScreen = NujoomScreen.ParentLogin },
                                        onNavigateChildLogin = { currentScreen = NujoomScreen.ChildLogin }
                                    )
                                }
                                is NujoomScreen.ParentLogin -> {
                                    ParentLoginScreen(
                                        vm = vm,
                                        onLoginSuccess = {
                                            currentScreen = NujoomScreen.ParentDashboard
                                            activeParentTab = ParentTab.Home
                                        },
                                        onNavigateRegister = { currentScreen = NujoomScreen.ParentRegister },
                                        onBack = { currentScreen = NujoomScreen.Welcome }
                                    )
                                }
                                is NujoomScreen.ParentRegister -> {
                                    ParentRegisterScreen(
                                        vm = vm,
                                        onRegisterSuccess = {
                                            currentScreen = NujoomScreen.ParentDashboard
                                            activeParentTab = ParentTab.Home
                                        },
                                        onNavigateLogin = { currentScreen = NujoomScreen.ParentLogin },
                                        onBack = { currentScreen = NujoomScreen.Welcome }
                                    )
                                }
                                is NujoomScreen.ChildLogin -> {
                                    ChildLoginScreen(
                                        vm = vm,
                                        onLoginSuccess = {
                                            currentScreen = NujoomScreen.ChildDashboard
                                            activeChildTab = ChildTab.Home
                                        },
                                        onNavigateRegister = { currentScreen = NujoomScreen.ChildRegister },
                                        onBack = { currentScreen = NujoomScreen.Welcome }
                                    )
                                }
                                is NujoomScreen.ChildRegister -> {
                                    ChildRegisterScreen(
                                        vm = vm,
                                        onRegisterSuccess = {
                                            currentScreen = NujoomScreen.ChildDashboard
                                            activeChildTab = ChildTab.Home
                                        },
                                        onNavigateLogin = { currentScreen = NujoomScreen.ChildLogin },
                                        onBack = { currentScreen = NujoomScreen.Welcome }
                                    )
                                }
                                is NujoomScreen.ParentDashboard -> {
                                    RenderParentDashboard(
                                        vm = vm,
                                        activeTab = activeParentTab,
                                        onTabChange = { activeParentTab = it },
                                        onNavigateChildSettings = { childId ->
                                            vm.selectChild(childId)
                                            activeParentTab = ParentTab.Tasks
                                        },
                                        onLogout = {
                                            vm.logOut()
                                            currentScreen = NujoomScreen.Welcome
                                        }
                                    )
                                }
                                is NujoomScreen.ChildDashboard -> {
                                    RenderChildDashboard(
                                        vm = vm,
                                        activeTab = activeChildTab,
                                        onTabChange = { activeChildTab = it },
                                        onLogout = {
                                            vm.logOut()
                                            currentScreen = NujoomScreen.Welcome
                                        }
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
    fun RenderParentDashboard(
        vm: NujoomViewModel,
        activeTab: ParentTab,
        onTabChange: (ParentTab) -> Unit,
        onNavigateChildSettings: (String) -> Unit,
        onLogout: () -> Unit
    ) {
        val userSessionType by vm.currentUserType.collectAsState()
        val currentLanguage by vm.currentLanguage.collectAsState()
        // Graceful checkout logout redirection
        LaunchedEffect(userSessionType) {
            if (userSessionType == null) {
                onLogout()
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(BgDarkPrimary)
            ) {
                when (activeTab) {
                    ParentTab.Home -> ParentMainDashboard(vm, onNavigateChildSettings)
                    ParentTab.Tasks -> ParentTasksScreen(vm)
                    ParentTab.Rewards -> ParentRewardsScreen(vm)
                    ParentTab.Religious -> ParentReligiousScreen(vm)
                }
            }

            // Beautiful standard M3 visual Bottom Tab Bar
            Surface(
                color = BgDarkSecondary,
                border = BorderStroke(1.dp, BgDarkCardBorder),
                modifier = Modifier.fillMaxWidth().height(68.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf(
                        Triple(ParentTab.Home, "🏠", if (currentLanguage == "ar") "الرئيسية" else "Home"),
                        Triple(ParentTab.Tasks, "📋", vm.translate("nav_tasks")),
                        Triple(ParentTab.Rewards, "🎁", vm.translate("nav_rewards")),
                        Triple(ParentTab.Religious, "🕌", if (currentLanguage == "ar") "الصلوات" else "Prayers")
                    )

                    tabs.forEach { (tab, emoji, label) ->
                        val isSelected = activeTab == tab
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onTabChange(tab) }
                                .padding(vertical = 4.dp)
                                .testTag("parent_tab_${tab.name.lowercase()}")
                        ) {
                            Text(
                                text = emoji,
                                fontSize = if (isSelected) 22.sp else 18.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                color = if (isSelected) AccentGold else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }

    @Composable
    fun RenderChildDashboard(
        vm: NujoomViewModel,
        activeTab: ChildTab,
        onTabChange: (ChildTab) -> Unit,
        onLogout: () -> Unit
    ) {
        val userSessionType by vm.currentUserType.collectAsState()
        // Graceful checkout logout redirection
        LaunchedEffect(userSessionType) {
            if (userSessionType == null) {
                onLogout()
            }
        }

        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .background(ChildBgStart)
            ) {
                when (activeTab) {
                    ChildTab.Home -> ChildHomeScreen(vm)
                    ChildTab.Prayers -> ChildPrayersScreen(vm)
                    ChildTab.Quran -> ChildQuranScreen(vm)
                    ChildTab.Rewards -> ChildRewardsScreen(vm)
                }
            }

            // Child special customized Bottom Tab Bar
            Surface(
                color = ChildBgEnd.copy(alpha = 0.95f),
                border = BorderStroke(1.dp, BgDarkCardBorder),
                modifier = Modifier.fillMaxWidth().height(68.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val tabs = listOf(
                        Triple(ChildTab.Home, "🏠", "مهامي"),
                        Triple(ChildTab.Prayers, "🕌", "صلاتي"),
                        Triple(ChildTab.Quran, "📖", "قرآني"),
                        Triple(ChildTab.Rewards, "🎁", "مكافئاتي")
                    )

                    tabs.forEach { (tab, emoji, label) ->
                        val isSelected = activeTab == tab
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .clickable { onTabChange(tab) }
                                .padding(vertical = 4.dp)
                                .testTag("child_tab_${tab.name.lowercase()}")
                        ) {
                            Text(
                                text = emoji,
                                fontSize = if (isSelected) 22.sp else 18.sp
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = label,
                                fontSize = 10.sp,
                                color = if (isSelected) AccentGold else Color.Gray,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
