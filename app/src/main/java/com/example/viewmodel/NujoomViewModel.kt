package com.example.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NujoomViewModel(application: Application) : AndroidViewModel(application) {
    val repository = NujoomRepository(application)

    // Current Session State
    val currentUserId = MutableStateFlow<String?>(repository.getCurrentUserId())
    val currentUserType = MutableStateFlow<String?>(repository.getCurrentUserType())
    val currentFamilyCode = MutableStateFlow<String?>(repository.getCurrentFamilyCode())

    // UI Configuration
    val currentLanguage = MutableStateFlow("ar") // Default Arabic-first
    val currentTheme = MutableStateFlow("dark") // Default Dark mode

    // Database dynamic flows
    val childrenList = currentFamilyCode.flatMapLatest { code ->
        if (code != null) repository.getChildrenFlow(code) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeChildId = MutableStateFlow<String?>(null)
    
    val activeChildTasks = activeChildId.combine(currentLanguage) { id, _ -> id }
        .flatMapLatest { id ->
            if (id != null) repository.getTasksForChildFlow(id, repository.getTodayKey()) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeChildPrayers = activeChildId.flatMapLatest { id ->
        if (id != null) repository.getPrayerFlow(id, repository.getTodayKey()) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeChildRecitations = activeChildId.flatMapLatest { id ->
        if (id != null) repository.getRecitationsForChildFlow(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeChildMessages = activeChildId.flatMapLatest { id ->
        if (id != null) repository.getMessagesForChildFlow(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeChildRedemptions = activeChildId.flatMapLatest { id ->
        if (id != null) repository.getRedemptionsForChildFlow(id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeFamilyRewards = currentFamilyCode.flatMapLatest { code ->
        if (code != null) repository.getRewardsFlow(code) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeFamilyRedemptions = currentFamilyCode.flatMapLatest { code ->
        if (code != null) repository.getRedemptionsForFamilyFlow(code) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val familyRecentTasks = currentFamilyCode.flatMapLatest { code ->
        if (code != null) repository.getRecentTasksByFamilyFlow(code) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeNotifications = currentUserId.flatMapLatest { uid ->
        if (uid != null) repository.getNotificationsFlow(uid) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Selected children context for details
    val selectedChild = activeChildId.flatMapLatest { id ->
        if (id != null) repository.getChildFlow(id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val childInboxMessages = activeChildMessages
    val childSessionData = selectedChild
    val familyLeaderboardEnabled = MutableStateFlow(true)

    // Form inputs & dynamic fields
    var parentRegName = mutableStateOf("")
    var parentRegEmail = mutableStateOf("")
    var parentRegPassword = mutableStateOf("")
    var parentPhone = mutableStateOf("")
    var termsAgreed = mutableStateOf(false)

    var parentLoginEmail = mutableStateOf(repository.getParentRememberEmail())
    var parentLoginPassword = mutableStateOf(repository.getParentRememberPassword())
    var parentRememberMe = mutableStateOf(repository.isParentRememberEnabled())

    var childRegFirstName = mutableStateOf("")
    var childRegLastName = mutableStateOf("")
    var childRegAge = mutableStateOf(8)
    var childRegPassword = mutableStateOf("")
    var childRegCode = mutableStateOf("")

    var selectedAvatarId = mutableStateOf("boy_glasses")
    var selectedAvatarFrame = mutableStateOf("#FFB800")

    // Modals control
    var showAppTourParent = mutableStateOf(false)
    var showAppTourChild = mutableStateOf(false)
    var generatedFamilyCodeToShow = mutableStateOf<String?>(null)

    // API Suggestions Loading States
    var isAiGenerating = MutableStateFlow(false)
    var aiSuggestedQuote = MutableStateFlow<String?>(null)

    init {
        // Initialize active child id if matching session child type
        viewModelScope.launch {
            if (currentUserType.value == "child") {
                activeChildId.value = currentUserId.value
            }
        }
    }

    fun translate(key: String): String {
        return NujoomTranslations.translate(key, currentLanguage.value)
    }

    // Locale & Theme Swapping
    fun toggleLanguage() {
        val newLang = if (currentLanguage.value == "ar") "en" else "ar"
        currentLanguage.value = newLang
        viewModelScope.launch {
            val code = currentFamilyCode.value
            if (code != null) {
                val family = repository.getFamily(code)
                if (family != null) {
                    repository.updateFamily(family.copy(language = newLang))
                }
            }
        }
    }

    fun toggleTheme() {
        val newTheme = if (currentTheme.value == "dark") "light" else "dark"
        currentTheme.value = newTheme
        viewModelScope.launch {
            val code = currentFamilyCode.value
            if (code != null) {
                val family = repository.getFamily(code)
                if (family != null) {
                    repository.updateFamily(family.copy(theme = newTheme))
                }
            }
        }
    }

    // Action Handlers
    fun parentRegister(onSuccess: (Family) -> Unit, onFailure: (String) -> Unit) {
        if (parentRegName.value.length < 3) {
            onFailure("الاسم يجب أن يكون 3 أحرف على الأقل / Name too short")
            return
        }
        if (!parentRegEmail.value.contains("@")) {
            onFailure("البريد الإلكتروني غير صالح / Invalid Email")
            return
        }
        if (parentRegPassword.value.length < 6) {
            onFailure("كلمة المرور يجب أن تكون 6 خانات على الأقل / Password too short")
            return
        }
        if (!termsAgreed.value) {
            onFailure("يجب الموافقة على الشروط أولاً / Must accept terms")
            return
        }

        viewModelScope.launch {
            val family = repository.registerFamily(
                parentRegName.value,
                parentRegEmail.value,
                parentRegPassword.value
            )
            if (family != null) {
                repository.loginSession(family.parentEmail, "parent", family.familyCode)
                currentUserId.value = family.parentEmail
                currentUserType.value = "parent"
                currentFamilyCode.value = family.familyCode
                generatedFamilyCodeToShow.value = family.familyCode
                showAppTourParent.value = true
                onSuccess(family)
            } else {
                onFailure("صيغة البريد مسجّلة أو خاطئة / Email already in use")
            }
        }
    }

    fun parentLogin(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            val family = repository.loginParent(parentLoginEmail.value, parentLoginPassword.value)
            if (family != null) {
                repository.loginSession(family.parentEmail, "parent", family.familyCode)
                repository.saveParentRememberMe(family.parentEmail, parentLoginPassword.value, parentRememberMe.value)
                currentUserId.value = family.parentEmail
                currentUserType.value = "parent"
                currentFamilyCode.value = family.familyCode
                currentLanguage.value = family.language
                currentTheme.value = family.theme
                onSuccess()
            } else {
                onFailure("خطأ في البيانات أو بريد غير مسجّل / Invalid email or password")
            }
        }
    }

    fun childRegister(onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        val code = childRegCode.value.trim().uppercase()
        if (childRegFirstName.value.length < 2) {
            onFailure("الاسم الأول قصير جداً / Name is too short")
            return
        }
        if (code.isEmpty()) {
            onFailure("يرجى إدخال كود العائلة / Missing Family Code")
            return
        }

        viewModelScope.launch {
            val family = repository.getFamily(code)
            if (family == null) {
                onFailure("الكود المدخل غير صحيح / Family code invalid")
                return@launch
            }

            val child = repository.registerChild(
                familyCode = code,
                firstName = childRegFirstName.value,
                lastName = childRegLastName.value,
                age = childRegAge.value,
                passwordHash = childRegPassword.value,
                avatarId = selectedAvatarId.value
            )

            if (child != null) {
                repository.loginSession(child.childId, "child", code)
                currentUserId.value = child.childId
                currentUserType.value = "child"
                currentFamilyCode.value = code
                activeChildId.value = child.childId
                showAppTourChild.value = true
                onSuccess()
            } else {
                onFailure("خطأ غير متوقع / Registration failed")
            }
        }
    }

    fun childLogin(familyCode: String, firstName: String, lastName: String, remember: Boolean, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        viewModelScope.launch {
            val child = repository.loginChild(familyCode.trim().uppercase(), firstName.trim(), lastName.trim())
            if (child != null) {
                repository.loginSession(child.childId, "child", child.familyCode)
                repository.saveChildRememberMe(firstName.trim(), lastName.trim(), child.familyCode, remember)
                currentUserId.value = child.childId
                currentUserType.value = "child"
                currentFamilyCode.value = child.familyCode
                activeChildId.value = child.childId
                onSuccess()
            } else {
                onFailure("الاسم أو كود العائلة غير صحيح / Invalid name or family code")
            }
        }
    }

    fun selectChild(childId: String) {
        activeChildId.value = childId
    }

    fun logOut() {
        repository.logoutSession()
        currentUserId.value = null
        currentUserType.value = null
        currentFamilyCode.value = null
        activeChildId.value = null
        // Reset forms
        parentRegName.value = ""
        parentRegEmail.value = ""
        parentRegPassword.value = ""
        parentLoginEmail.value = ""
        parentLoginPassword.value = ""
        childRegFirstName.value = ""
        childRegCode.value = ""
    }

    fun checkInitialSession(
        onSuccessParent: () -> Unit,
        onSuccessChild: () -> Unit,
        onNoSession: () -> Unit
    ) {
        val uid = currentUserId.value
        val type = currentUserType.value
        if (uid != null && type != null) {
            if (type == "parent") {
                onSuccessParent()
            } else {
                activeChildId.value = uid
                onSuccessChild()
            }
        } else {
            onNoSession()
        }
    }

    fun markMessageAsRead(messageId: String) {
        viewModelScope.launch {
            val cid = activeChildId.value
            if (cid != null) {
                repository.markAllMessagesAsRead(cid)
            }
        }
    }

    fun addCustomTask(nameAr: String, nameEn: String, emoji: String, stars: Int, requiresProof: Boolean, frequency: String) {
        viewModelScope.launch {
            val cid = activeChildId.value
            val fcode = currentFamilyCode.value
            if (cid != null && fcode != null) {
                repository.addCustomTask(cid, fcode, nameAr, nameEn, emoji, stars, requiresProof, frequency)
            }
        }
    }

    fun updateTask(task: Task, newStatus: String, proofPath: String? = null) {
        viewModelScope.launch {
            repository.updateTaskStatus(task, newStatus, proofPath)
            // If they are checking items
            if (newStatus == "completed" && !task.requiresProof) {
                // Instantly approve non-proof tasks for child if they do them
                repository.approveTask(task.taskId, "system")
            }
        }
    }

    fun approveTask(taskId: String) {
        viewModelScope.launch {
            repository.approveTask(taskId, currentUserId.value ?: "parent")
        }
    }

    fun rejectTask(taskId: String, reason: String) {
        viewModelScope.launch {
            repository.rejectTask(taskId, currentUserId.value ?: "parent", reason)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    // Prayers
    fun togglePrayer(prayerName: String, value: Boolean) {
        viewModelScope.launch {
            val cid = activeChildId.value
            if (cid != null) {
                repository.togglePrayer(cid, repository.getTodayKey(), prayerName, value)
            }
        }
    }

    // Recitations
    fun adjustMemorizeAyahs(diff: Int) {
        val cid = activeChildId.value ?: return
        viewModelScope.launch {
            val child = repository.getChild(cid)
            if (child != null) {
                val currentSurahName = child.memorizeSurah
                val surahInfo = NujoomConstants.QURAN_SURAHS.find { it.nameAr == currentSurahName || it.nameEn == currentSurahName }
                val maxAyas = surahInfo?.ayahs ?: 30
                val newCount = (child.memorizeAyahs + diff).coerceIn(1, maxAyas)
                repository.updateChild(child.copy(memorizeAyahs = newCount))
            }
        }
    }

    fun setChildMemorizeSurah(childId: String, surahName: String) {
        viewModelScope.launch {
            val child = repository.getChild(childId)
            if (child != null) {
                val surahInfo = NujoomConstants.QURAN_SURAHS.find { it.nameAr == surahName || it.nameEn == surahName }
                val maxAyas = surahInfo?.ayahs ?: 30
                val targetAyas = child.memorizeAyahs.coerceIn(1, maxAyas)
                repository.updateChild(child.copy(memorizeSurah = surahName, memorizeAyahs = targetAyas))
            }
        }
    }

    fun submitRecitation(surah: String, duration: String, path: String, sheikhName: String? = null) {
        viewModelScope.launch {
            val cid = activeChildId.value
            if (cid != null) {
                repository.addRecitation(cid, surah, duration, path, sheikhName)
                
                if (sheikhName != null) {
                    launch {
                        kotlinx.coroutines.delay(4000) // 4 seconds delay
                        val currentList = activeChildRecitations.value
                        val targetRec = currentList
                            .filter { it.surahName == surah && it.sheikhName == sheikhName && it.sheikhComment == null }
                            .maxByOrNull { it.recordedAt }
                        
                        if (targetRec != null) {
                            val rating = (4..5).random()
                            val comment = when (sheikhName) {
                                "الشيخ عبد الباسط عبد الصمد" -> listOf(
                                    "ما شاء الله تبارك الرحمن! صوت خاشع يذكرنا بروعة الترتيل العذب. تلاوة ممتازة جداً يا بني، واصل المداومة وحفظ كتاب الله العزيز.",
                                    "أحسنت القراءة يا بني العزيز! تلاوة مجودة وبصوت جميل ومؤثر للغاية. استمر في التدرب والترتيل لتكون من أهل القرآن وسفرائه."
                                ).random()
                                "الشيخ مشاري بن راشد العفاسي" -> listOf(
                                    "قراءة رائعة ومميزة جداً بصوت عذب يبعث السكينة والخشوع في القلوب! أحسنت الالتزام بأحكام الغنة والترتيل، بارك الله فيك ونفع بك الحاضرين.",
                                    "تلاوة غاية في الجمال والخشوع يا بني الحبيب. نبرتك نقية وقراءتك صحيحة ومؤثرة للغاية، استمر في حفظ كتاب الله والمواظبة على تلاوته يومياً."
                                ).random()
                                "الشيخ محمود خليل الحصري" -> listOf(
                                    "أحسنت وأقنعت يا قرة عيني! تلاوة منضبطة بمخارج الحروف وقواعد التجويد الصحيحة بشكل سليم ومثمر للغاية. بارك الله في والديك وصنيعهما المعطاء.",
                                    "ما شاء الله، قراءة مجودة سليمة وصحيحة ومتقنة تماماً على سنن وأصول الترتيل والتجويد الدقيق. واصل تلاوتك اليومية وثبّت حفظك."
                                ).random()
                                else -> "تلاوة خاشعة وجميلة جداً، بارك الله فيك ونفع بك الأمة."
                            }
                            repository.reviewRecitation(targetRec.recitationId, "sheikh_${sheikhName}", comment, rating)
                        }
                    }
                }
            }
        }
    }

    fun reviewRecitation(recitationId: String, comment: String, stars: Int) {
        viewModelScope.launch {
            repository.reviewRecitation(recitationId, currentUserId.value ?: "parent", comment, stars)
        }
    }

    // Rewards
    fun addReward(nameAr: String, nameEn: String, emoji: String, stars: Int, category: String) {
        viewModelScope.launch {
            val code = currentFamilyCode.value
            if (code != null) {
                repository.addReward(code, nameAr, nameEn, emoji, stars, category)
            }
        }
    }

    fun toggleRewardAvailability(reward: Reward) {
        viewModelScope.launch {
            repository.updateReward(reward.copy(isAvailable = !reward.isAvailable))
        }
    }

    fun deleteReward(rewardId: String) {
        viewModelScope.launch {
            repository.deleteReward(rewardId)
        }
    }

    fun requestRewardRedemption(reward: Reward, onComplete: (Boolean) -> Unit) {
        viewModelScope.launch {
            val cid = activeChildId.value
            if (cid != null) {
                val success = repository.requestRedemption(cid, reward)
                onComplete(success)
            } else {
                onComplete(false)
            }
        }
    }

    fun resolveRedemption(redemptionId: String, status: String, reason: String? = null) {
        viewModelScope.launch {
            repository.resolveRedemption(redemptionId, status, currentUserId.value ?: "parent", reason)
        }
    }

    fun sendShoutout(message: String) {
        viewModelScope.launch {
            val cid = activeChildId.value
            val parentName = repository.getFamily(currentFamilyCode.value.orEmpty())?.parentName ?: "الأهل"
            if (cid != null && message.trim().isNotEmpty()) {
                repository.sendEncouragingMessage(cid, message, parentName)
            }
        }
    }

    fun clearNotifications() {
        viewModelScope.launch {
            val uid = currentUserId.value
            if (uid != null) {
                repository.markNotificationsRead(uid)
            }
        }
    }

    fun submitTaskProof(taskId: String) {
        viewModelScope.launch {
            val tasksList = activeChildTasks.value
            val taskObj = tasksList.firstOrNull { it.taskId == taskId }
            if (taskObj != null) {
                updateTask(taskObj, "awaiting-review", "families/${currentFamilyCode.value.orEmpty()}/proofs/${taskId}_proof.jpg")
            }
        }
    }

    fun completeDirectTask(taskId: String) {
        viewModelScope.launch {
            val tasksList = activeChildTasks.value
            val taskObj = tasksList.firstOrNull { it.taskId == taskId }
            if (taskObj != null) {
                updateTask(taskObj, "completed")
            }
        }
    }

    fun markPrayerCompleted(prayerName: String) {
        togglePrayer(prayerName, true)
    }

    fun requestRedemption(rewardId: String) {
        viewModelScope.launch {
            val rewardObj = activeFamilyRewards.value.firstOrNull { it.rewardId == rewardId }
            if (rewardObj != null) {
                requestRewardRedemption(rewardObj) { success -> }
            }
        }
    }

    // --- Gemini Suggestion API call using OkHttp ---
    fun fetchAiEncouragement(childName: String, lastTaskName: String) {
        val prompt = if (currentLanguage.value == "ar") {
            "اكتب عبارة تشجيعية تربوية قصيرة جداً (سطر واحد فقط) للطفل $childName بمناسبة إنجاز مهمة '$lastTaskName'. يجب أن تكون محفزة، صادقة، بدون تعقيد، وبلهجة فيها الحب والدعم."
        } else {
            "Write a very short (one sentence) warm educational parenting encouragement phrase for my child $childName that completed '$lastTaskName'. Genuine and loving tone."
        }

        viewModelScope.launch(Dispatchers.IO) {
            isAiGenerating.value = true
            try {
                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey == "MY_GEMINI_API_KEY" || apiKey.isEmpty()) {
                    // Fallback to cute static text if key not fully customized yet
                    withContext(Dispatchers.Main) {
                        aiSuggestedQuote.value = if (currentLanguage.value == "ar") {
                            "بطل متميز في سماء نجوم! فخور جداً بجميل مثابرتك واصل التألق 🌟"
                        } else {
                            "A sparkling star in the sky of Nujoom! So proud of your amazing determination! 🌟"
                        }
                    }
                    return@launch
                }

                val client = OkHttpClient.Builder()
                    .connectTimeout(15, TimeUnit.SECONDS)
                    .readTimeout(15, TimeUnit.SECONDS)
                    .build()

                val mediaType = "application/json; charset=utf-8".toMediaType()
                val jsonBody = JSONObject().apply {
                    put("contents", org.json.JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", org.json.JSONArray().apply {
                                put(JSONObject().apply {
                                    put("text", prompt)
                                })
                            })
                        })
                    })
                }

                val request = Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=$apiKey")
                    .post(jsonBody.toString().toRequestBody(mediaType))
                    .build()

                client.newCall(request).execute().use { response ->
                    val respStr = response.body?.string()
                    if (response.isSuccessful && !respStr.isNullOrEmpty()) {
                        val root = JSONObject(respStr)
                        val candidates = root.getJSONArray("candidates")
                        val text = candidates.getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")
                            .trim()
                        
                        withContext(Dispatchers.Main) {
                            aiSuggestedQuote.value = text
                        }
                    } else {
                        throw Exception("Failed with code: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    aiSuggestedQuote.value = if (currentLanguage.value == "ar") {
                        "يا لك من بطل رائع! كلما طرقت باب الهمة، سطع اسمك بين النجوم ✨ f"
                    } else {
                        "What an outstanding hero! Keep shining bright among the stars! ✨"
                    }
                }
            } finally {
                isAiGenerating.value = false
            }
        }
    }
}
