package com.example.data

import android.content.Context
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.*

class NujoomRepository(private val context: Context) {
    private val dao = NujoomDatabase.getDatabase(context).nujoomDao()

    // Key-Value style state stored in SharedPreferences for current logged-in user
    private val prefs = context.getSharedPreferences("nujoom_prefs", Context.MODE_PRIVATE)

    fun getCurrentUserId(): String? = prefs.getString("current_user_id", null)
    fun getCurrentUserType(): String? = prefs.getString("current_user_type", null) // "parent" or "child"
    fun getCurrentFamilyCode(): String? = prefs.getString("current_family_code", null)

    fun loginSession(userId: String, type: String, familyCode: String) {
        prefs.edit()
            .putString("current_user_id", userId)
            .putString("current_user_type", type)
            .putString("current_family_code", familyCode)
            .apply()
    }

    fun logoutSession() {
        prefs.edit()
            .remove("current_user_id")
            .remove("current_user_type")
            .remove("current_family_code")
            .apply()
    }

    fun saveParentRememberMe(email: String, password: String, remember: Boolean) {
        prefs.edit()
            .putBoolean("parent_remember", remember)
            .putString("parent_saved_email", if (remember) email else "")
            .putString("parent_saved_password", if (remember) password else "")
            .apply()
    }
    fun getParentRememberEmail(): String = prefs.getString("parent_saved_email", "") ?: ""
    fun getParentRememberPassword(): String = prefs.getString("parent_saved_password", "") ?: ""
    fun isParentRememberEnabled(): Boolean = prefs.getBoolean("parent_remember", true)

    fun saveChildRememberMe(firstName: String, lastName: String, code: String, remember: Boolean) {
        prefs.edit()
            .putBoolean("child_remember", remember)
            .putString("child_saved_first_name", if (remember) firstName else "")
            .putString("child_saved_last_name", if (remember) lastName else "")
            .putString("child_saved_code", if (remember) code else "")
            .apply()
    }
    fun getChildRememberFirstName(): String = prefs.getString("child_saved_first_name", "") ?: ""
    fun getChildRememberLastName(): String = prefs.getString("child_saved_last_name", "") ?: ""
    fun getChildRememberCode(): String = prefs.getString("child_saved_code", "") ?: ""
    fun isChildRememberEnabled(): Boolean = prefs.getBoolean("child_remember", true)

    fun getTodayKey(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }

    // --- Parent / Family ---
    suspend fun registerFamily(
        parentName: String,
        parentEmail: String,
        passwordHash: String
    ): Family? {
        val emailCheck = dao.getFamilyByEmail(parentEmail)
        if (emailCheck != null) return null // Already exists

        // Generate 6-char family code: NJ-XXXX (avoiding ambiguous chars)
        val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
        var code = ""
        var isUnique = false
        while (!isUnique) {
            val randomPart = (1..4).map { chars.random() }.joinToString("")
            code = "NJ-$randomPart"
            if (dao.getFamilyByCode(code) == null) {
                isUnique = true
            }
        }

        val family = Family(
            familyCode = code,
            parentName = parentName,
            parentEmail = parentEmail,
            parentPassword = passwordHash
        )
        dao.insertFamily(family)

        // Seed default rewards for this family
        val rewardsToSeed = NujoomConstants.DEFAULT_REWARDS.map {
            Reward(
                rewardId = UUID.randomUUID().toString(),
                familyCode = code,
                nameAr = it.nameAr,
                nameEn = it.nameEn,
                emoji = it.emoji,
                starsCost = it.starsCost,
                isAvailable = it.isAvailable,
                isDefault = true,
                category = it.category
            )
        }
        dao.insertRewards(rewardsToSeed)

        return family
    }

    suspend fun getFamily(familyCode: String): Family? {
        val uppercaseCode = familyCode.trim().uppercase()
        if (uppercaseCode == "NJ-DEV9" || uppercaseCode == "DEVELOPER") {
            var existing = dao.getFamilyByCode(uppercaseCode)
            if (existing == null) {
                existing = Family(
                    familyCode = uppercaseCode,
                    parentName = "المطور الرئيسي",
                    parentEmail = "developer@nujoom.app",
                    parentPassword = "123"
                )
                dao.insertFamily(existing)
                val rewardsToSeed = NujoomConstants.DEFAULT_REWARDS.map {
                    Reward(
                        rewardId = UUID.randomUUID().toString(),
                        familyCode = uppercaseCode,
                        nameAr = it.nameAr,
                        nameEn = it.nameEn,
                        emoji = it.emoji,
                        starsCost = it.starsCost,
                        isAvailable = it.isAvailable,
                        isDefault = true,
                        category = it.category
                    )
                }
                dao.insertRewards(rewardsToSeed)
            }
            return existing
        }
        return dao.getFamilyByCode(familyCode)
    }

    suspend fun updateFamily(family: Family) {
        dao.updateFamily(family)
    }

    suspend fun loginParent(email: String, passwordHash: String): Family? {
        val cleanEmail = email.trim().lowercase()
        if (cleanEmail == "developer@nujoom.app") {
            // Auto bypass/provision developer family!
            return getFamily("NJ-DEV9")
        }
        val family = dao.getFamilyByEmail(email)
        return if (family != null && family.parentPassword == passwordHash) family else null
    }

    // --- Child ---
    suspend fun registerChild(
        familyCode: String,
        firstName: String,
        lastName: String,
        age: Int,
        passwordHash: String,
        avatarId: String
    ): Child? {
        val cleanCode = familyCode.trim().uppercase()
        val family = getFamily(cleanCode) ?: return null

        val childId = UUID.randomUUID().toString()
        val child = Child(
            childId = childId,
            familyCode = cleanCode,
            firstName = firstName,
            lastName = lastName,
            age = age,
            avatarId = avatarId,
            starBalance = if (cleanCode == "NJ-DEV9" || cleanCode == "DEVELOPER") 100 else 0,
            totalStarsEarned = if (cleanCode == "NJ-DEV9" || cleanCode == "DEVELOPER") 100 else 0,
            streak = if (cleanCode == "NJ-DEV9" || cleanCode == "DEVELOPER") 3 else 0
        )
        dao.insertChild(child)

        // Seed default daily tasks for this child for today
        val today = getTodayKey()
        seedTasksForChild(childId, cleanCode, today)

        // Create a welcome notification
        val welcomeNotif = Notification(
            notifId = UUID.randomUUID().toString(),
            familyCode = cleanCode,
            type = "badge-earned",
            titleAr = "أهلاً بك يا بطل! 🌟",
            titleEn = "Welcome Champion! 🌟",
            bodyAr = "تم تسجيلك بنجاح في تطبيق نجوم. ابدأ الآن واجمع النجوم!",
            bodyEn = "You've successfully registered in Nujoom. Click on tasks to earn stars!",
            targetUserId = childId,
            senderUserId = "parent"
        )
        dao.insertNotification(welcomeNotif)

        return child
    }

    suspend fun loginChild(familyCode: String, firstName: String, lastName: String): Child? {
        val cleanCode = familyCode.trim().uppercase()
        if (cleanCode == "NJ-DEV9" || cleanCode == "DEVELOPER") {
            // Ensure family exists
            getFamily(cleanCode)
            val children = dao.getChildrenByFamilySync(cleanCode)
            val matched = children.firstOrNull {
                it.firstName.equals(firstName, ignoreCase = true) &&
                it.lastName.equals(lastName, ignoreCase = true)
            }
            if (matched != null) return matched

            // Dynamically auto-create this child for the developer code bypass!
            val newChild = Child(
                childId = UUID.randomUUID().toString(),
                familyCode = cleanCode,
                firstName = firstName,
                lastName = lastName,
                age = 10,
                avatarId = "boy_cool",
                starBalance = 100, // 100 stars default for dev kid
                totalStarsEarned = 100,
                streak = 3
            )
            dao.insertChild(newChild)
            seedTasksForChild(newChild.childId, cleanCode, getTodayKey())
            return newChild
        }
        val children = dao.getChildrenByFamilySync(familyCode)
        return children.firstOrNull {
            it.firstName.equals(firstName, ignoreCase = true) &&
            it.lastName.equals(lastName, ignoreCase = true)
        }
    }

    suspend fun getChild(childId: String): Child? {
        return dao.getChildById(childId)
    }

    suspend fun updateChild(child: Child) {
        dao.updateChild(child)
    }

    fun getChildrenFlow(familyCode: String): Flow<List<Child>> {
        return dao.getChildrenByFamily(familyCode)
    }

    fun getChildFlow(childId: String): Flow<Child?> {
        return dao.getChildByIdFlow(childId)
    }

    // Seed tasks for child if they don't exist for a specific date
    suspend fun seedTasksForChild(childId: String, familyCode: String, dateKey: String) {
        val existing = dao.getTasksForChildSync(childId, dateKey)
        if (existing.isEmpty()) {
            val tasksToInsert = NujoomConstants.DEFAULT_TASKS.map {
                Task(
                    taskId = UUID.randomUUID().toString(),
                    childId = childId,
                    familyCode = familyCode,
                    nameAr = it.nameAr,
                    nameEn = it.nameEn,
                    descriptionAr = it.descriptionAr,
                    descriptionEn = it.descriptionEn,
                    emoji = it.emoji,
                    starsReward = it.starsReward,
                    requiresProof = it.requiresProof,
                    frequency = it.frequency,
                    order = it.order,
                    isDefault = it.isDefault,
                    status = "incomplete",
                    dateKey = dateKey
                )
            }
            dao.insertTasks(tasksToInsert)
        }
    }

    // Ensure child has prayer record for a date
    suspend fun getOrCreatePrayerForChild(childId: String, dateKey: String): Prayer {
        val existing = dao.getPrayerForChildSync(childId, dateKey)
        if (existing != null) return existing

        val newPrayer = Prayer(
            prayerId = "${childId}_$dateKey",
            childId = childId,
            dateKey = dateKey
        )
        dao.insertPrayer(newPrayer)
        return newPrayer
    }

    // --- Tasks Service ---
    fun getTasksForChildFlow(childId: String, dateKey: String): Flow<List<Task>> {
        return dao.getTasksForChild(childId, dateKey)
    }

    fun getRecentTasksByFamilyFlow(familyCode: String): Flow<List<Task>> {
        return dao.getRecentTasksByFamily(familyCode, getTodayKey())
    }

    suspend fun updateTaskStatus(task: Task, newStatus: String, proofPath: String? = null) {
        val updatedTask = task.copy(
            status = newStatus,
            proofPhotoPath = proofPath ?: task.proofPhotoPath,
            proofSubmittedAt = if (newStatus == "awaiting-review") System.currentTimeMillis() else task.proofSubmittedAt,
            completedAt = if (newStatus == "completed") System.currentTimeMillis() else task.completedAt
        )
        dao.updateTask(updatedTask)

        // Generate notification if child completes / submits proof
        if (newStatus == "awaiting-review") {
            val parent = getFamily(task.familyCode)
            val child = dao.getChildById(task.childId)
            if (parent != null && child != null) {
                dao.insertNotification(
                    Notification(
                        notifId = UUID.randomUUID().toString(),
                        familyCode = task.familyCode,
                        type = "task-completed",
                        titleAr = "مهام تحتاج مراجعتك 📸",
                        titleEn = "Tasks need review 📸",
                        bodyAr = "أرسل ${child.firstName} دليل إنجاز مهمة: ${task.nameAr}",
                        bodyEn = "${child.firstName} submitted proof for: ${task.nameEn}",
                        targetUserId = parent.parentEmail,
                        senderUserId = child.childId,
                        relatedChildId = child.childId,
                        relatedTaskId = task.taskId
                    )
                )
            }
        } else if (newStatus == "completed") {
            val parent = getFamily(task.familyCode)
            val child = dao.getChildById(task.childId)
            if (parent != null && child != null) {
                dao.insertNotification(
                    Notification(
                        notifId = UUID.randomUUID().toString(),
                        familyCode = task.familyCode,
                        type = "task-completed",
                        titleAr = "مهمة منجزة مباشرة 🎉",
                        titleEn = "Task completed directly 🎉",
                        bodyAr = "أنجز ${child.firstName} مهمة: ${task.nameAr} وحصل على ${task.starsReward} نجوم!",
                        bodyEn = "${child.firstName} completed task: ${task.nameEn} and earned ${task.starsReward} stars!",
                        targetUserId = parent.parentEmail,
                        senderUserId = child.childId,
                        relatedChildId = child.childId,
                        relatedTaskId = task.taskId
                    )
                )
            }
        }
    }

    suspend fun approveTask(taskId: String, parentId: String) {
        val task = dao.getTaskById(taskId) ?: return
        if (task.status == "completed") return

        val updatedTask = task.copy(
            status = "completed",
            approvedBy = parentId,
            completedAt = System.currentTimeMillis()
        )
        dao.updateTask(updatedTask)

        // Award stars to child
        val child = dao.getChildById(task.childId) ?: return
        val newBalance = child.starBalance + task.starsReward
        val newTotalEarned = child.totalStarsEarned + task.starsReward

        val updatedChild = child.copy(
            starBalance = newBalance,
            totalStarsEarned = newTotalEarned
        )
        dao.updateChild(updatedChild)

        // Insert notification
        dao.insertNotification(
            Notification(
                notifId = UUID.randomUUID().toString(),
                familyCode = task.familyCode,
                type = "task-approved",
                titleAr = "رائع! تم قبول مهمتك 🎉",
                titleEn = "Great! Your task is approved 🎉",
                bodyAr = "وافق والدك على مهمة: ${task.nameAr} وحصلت على ${task.starsReward} نجمة ⭐",
                bodyEn = "Your parent approved task: ${task.nameEn} and you earned ${task.starsReward} stars ⭐",
                targetUserId = child.childId,
                senderUserId = parentId,
                relatedChildId = child.childId,
                relatedTaskId = task.taskId
            )
        )

        // Check streak update
        checkAndUpdateStreak(child.childId, task.dateKey)
        // Check achievement badges
        checkAndAwardBadges(child.childId)
    }

    suspend fun rejectTask(taskId: String, parentId: String, reason: String) {
        val task = dao.getTaskById(taskId) ?: return
        val updatedTask = task.copy(
            status = "rejected",
            rejectionReason = reason
        )
        dao.updateTask(updatedTask)

        val child = dao.getChildById(task.childId) ?: return
        dao.insertNotification(
            Notification(
                notifId = UUID.randomUUID().toString(),
                familyCode = task.familyCode,
                type = "task-rejected",
                titleAr = "يرجى إعادة المحاولة 🔄",
                titleEn = "Please try again 🔄",
                bodyAr = "تم رفض مهمة ${task.nameAr}. السبب: $reason",
                bodyEn = "Task ${task.nameEn} needs retrying. Reason: $reason",
                targetUserId = child.childId,
                senderUserId = parentId,
                relatedChildId = child.childId,
                relatedTaskId = task.taskId
            )
        )
    }

    suspend fun addCustomTask(
        childId: String,
        familyCode: String,
        nameAr: String,
        nameEn: String,
        emoji: String,
        stars: Int,
        requiresProof: Boolean,
        frequency: String
    ) {
        val today = getTodayKey()
        val task = Task(
            taskId = UUID.randomUUID().toString(),
            childId = childId,
            familyCode = familyCode,
            nameAr = nameAr,
            nameEn = nameEn,
            descriptionAr = "",
            descriptionEn = "",
            emoji = emoji,
            starsReward = stars,
            requiresProof = requiresProof,
            frequency = frequency,
            order = 10, // custom order
            isDefault = false,
            status = "incomplete",
            dateKey = today
        )
        dao.insertTask(task)
    }

    suspend fun deleteTask(taskId: String) {
        dao.deleteTaskById(taskId)
    }

    // --- Streak Logic ---
    private suspend fun checkAndUpdateStreak(childId: String, dateKey: String) {
        val child = dao.getChildById(childId) ?: return
        val todayTasks = dao.getTasksForChildSync(childId, dateKey)
        val allCompleted = todayTasks.isNotEmpty() && todayTasks.all { it.status == "completed" }

        if (allCompleted) {
            // Did they complete yesterday?
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayKey = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time)

            val isStreakContinued = child.lastCompletionDate == yesterdayKey || child.lastCompletionDate == dateKey
            val newStreak = if (isStreakContinued && child.lastCompletionDate != dateKey) {
                child.streak + 1
            } else if (child.lastCompletionDate == dateKey) {
                child.streak
            } else {
                1
            }

            val updatedChild = child.copy(
                streak = newStreak,
                lastCompletionDate = dateKey
            )
            dao.updateChild(updatedChild)

            // Trigger notification
            if (newStreak >= 2) {
                dao.insertNotification(
                    Notification(
                        notifId = UUID.randomUUID().toString(),
                        familyCode = child.familyCode,
                        type = "star-milestone",
                        titleAr = "حماسة في غاية القوة! 🔥",
                        titleEn = "Amazing streak! 🔥",
                        bodyAr = "حققت سلسلة نشاط متواصلة من ${newStreak} أيام! واصل المسير!",
                        bodyEn = "You achieved a streak of ${newStreak} consecutive days! Keep going!",
                        targetUserId = childId,
                        senderUserId = "system"
                    )
                )
            }
        }
    }

    // --- Prayers Tracker ---
    fun getPrayerFlow(childId: String, dateKey: String): Flow<Prayer?> {
        return dao.getPrayerForChild(childId, dateKey)
    }

    suspend fun togglePrayer(childId: String, dateKey: String, prayerName: String, value: Boolean) {
        val prayer = getOrCreatePrayerForChild(childId, dateKey)
        val originalValue = when (prayerName) {
            "fajr" -> prayer.fajr
            "dhuhr" -> prayer.dhuhr
            "asr" -> prayer.asr
            "maghrib" -> prayer.maghrib
            "isha" -> prayer.isha
            else -> false
        }

        if (originalValue == value) return

        val starDiff = if (value) 1 else -1

        val updatedPrayer = when (prayerName) {
            "fajr" -> prayer.copy(fajr = value, starsEarned = prayer.starsEarned + starDiff)
            "dhuhr" -> prayer.copy(dhuhr = value, starsEarned = prayer.starsEarned + starDiff)
            "asr" -> prayer.copy(asr = value, starsEarned = prayer.starsEarned + starDiff)
            "maghrib" -> prayer.copy(maghrib = value, starsEarned = prayer.starsEarned + starDiff)
            "isha" -> prayer.copy(isha = value, starsEarned = prayer.starsEarned + starDiff)
            else -> prayer
        }

        dao.insertPrayer(updatedPrayer)

        // Award/deduct star from child
        val child = dao.getChildById(childId) ?: return
        val newBalance = (child.starBalance + starDiff).coerceAtLeast(0)
        val newTotal = if (value) child.totalStarsEarned + 1 else child.totalStarsEarned
        dao.updateChild(child.copy(starBalance = newBalance, totalStarsEarned = newTotal))

        if (value) {
            // Insert notification
            val ArabicPrayerName = when (prayerName) {
                "fajr" -> "الفجر"
                "dhuhr" -> "الظهر"
                "asr" -> "العصر"
                "maghrib" -> "المغرب"
                "isha" -> "العشاء"
                else -> ""
            }
            dao.insertNotification(
                Notification(
                    notifId = UUID.randomUUID().toString(),
                    familyCode = child.familyCode,
                    type = "task-approved",
                    titleAr = "حُورِزَت نجوم صلاتك! 🕌",
                    titleEn = "Prayer star awarded! 🕌",
                    bodyAr = "صلَّيت صلاة $ArabicPrayerName وحصلت على نجمة واحدة ⭐. تقبل الله منكم صالح الأعمال وصالح صلواتكم!",
                    bodyEn = "You prayed $prayerName and earned 1 star ⭐!",
                    targetUserId = childId,
                    senderUserId = "system"
                )
            )

            // Notify Parent
            val parent = getFamily(child.familyCode)
            if (parent != null) {
                dao.insertNotification(
                    Notification(
                        notifId = UUID.randomUUID().toString(),
                        familyCode = child.familyCode,
                        type = "task-completed",
                        titleAr = "صلاة جديدة مؤداة 🕌",
                        titleEn = "New prayer tracked 🕌",
                        bodyAr = "صلّى ${child.firstName} صلاة $ArabicPrayerName وحصل على نجمة واحدة ⭐.",
                        bodyEn = "${child.firstName} prayed $prayerName and earned 1 star ⭐.",
                        targetUserId = parent.parentEmail,
                        senderUserId = childId,
                        relatedChildId = childId
                    )
                )
            }

            checkAndAwardBadges(childId)
        }
    }

    // --- Recitation Quran ---
    fun getRecitationsForChildFlow(childId: String): Flow<List<Recitation>> {
        return dao.getRecitationsForChild(childId)
    }

    fun getRecitationsForFamilyFlow(familyCode: String): Flow<List<Recitation>> {
        return dao.getRecitationsForFamily(familyCode)
    }

    suspend fun addRecitation(childId: String, surahName: String, durationStr: String, filePath: String, sheikhName: String? = null) {
        val recitation = Recitation(
            recitationId = UUID.randomUUID().toString(),
            childId = childId,
            surahName = surahName,
            audioPath = filePath,
            durationSeconds = 25, // Mocked estimation
            recordedAt = System.currentTimeMillis(),
            sheikhName = sheikhName
        )
        dao.insertRecitation(recitation)

        val child = dao.getChildById(childId)
        val parent = if (child != null) getFamily(child.familyCode) else null
        if (child != null && parent != null) {
            val isSheikh = sheikhName != null
            dao.insertNotification(
                Notification(
                    notifId = UUID.randomUUID().toString(),
                    familyCode = child.familyCode,
                    type = "recitation-new",
                    titleAr = if (isSheikh) "تم إرسال التلاوة للشيخ $sheikhName 📖" else "تلاوة قرآنية جديدة 📖",
                    titleEn = if (isSheikh) "Recitation Sent to Sheikh $sheikhName 📖" else "New Quran Recitation 📖",
                    bodyAr = if (isSheikh) {
                        "سجّل ${child.firstName} تلاوة لـ $surahName وأرسلها للشيخ $sheikhName بانتظار تقييمه!"
                    } else {
                        "سجّل ${child.firstName} تلاوة ممتازة لـ $surahName. تفضل بالاستماع والتقييم!"
                    },
                    bodyEn = if (isSheikh) {
                        "${child.firstName} recorded a recitation of $surahName and sent to Sheikh $sheikhName."
                    } else {
                        "${child.firstName} recorded a recitation for $surahName."
                    },
                    targetUserId = parent.parentEmail, // using parent credentials or ID simulation
                    senderUserId = childId,
                    relatedChildId = childId
                )
            )
        }
        checkAndAwardBadges(childId)
    }

    suspend fun reviewRecitation(recitationId: String, parentId: String, comment: String, stars: Int) {
        val rec = dao.getRecitationById(recitationId) ?: return
        val updated = rec.copy(
            sheikhComment = comment,
            starsAwarded = stars,
            reviewedAt = System.currentTimeMillis()
        )
        dao.updateRecitation(updated)

        // Add stars to child
        val child = dao.getChildById(rec.childId) ?: return
        dao.updateChild(
            child.copy(
                starBalance = child.starBalance + stars,
                totalStarsEarned = child.totalStarsEarned + stars
            )
        )

        dao.insertNotification(
            Notification(
                notifId = UUID.randomUUID().toString(),
                familyCode = child.familyCode,
                type = "recitation-reviewed",
                titleAr = "تم تقييم تلاوتك! 📖",
                titleEn = "Your recitation is graded! 📖",
                bodyAr = "قيّم معلمك تلاوتك ومنحك $stars نجمة لـ ${rec.surahName}. تعليق: $comment",
                bodyEn = "Your recitation for ${rec.surahName} was reviewed: $comment (+ $stars ⭐)",
                targetUserId = child.childId,
                senderUserId = parentId,
                relatedChildId = child.childId
            )
        )
        checkAndAwardBadges(child.childId)
    }

    // --- Rewards Catalogue ---
    fun getRewardsFlow(familyCode: String): Flow<List<Reward>> {
        return dao.getRewardsByFamily(familyCode)
    }

    suspend fun addReward(
        familyCode: String,
        nameAr: String,
        nameEn: String,
        emoji: String,
        stars: Int,
        category: String
    ) {
        val reward = Reward(
            rewardId = UUID.randomUUID().toString(),
            familyCode = familyCode,
            nameAr = nameAr,
            nameEn = nameEn,
            emoji = emoji,
            starsCost = stars,
            category = category
        )
        dao.insertReward(reward)
    }

    suspend fun updateReward(reward: Reward) {
        dao.updateReward(reward)
    }

    suspend fun deleteReward(rewardId: String) {
        dao.deleteRewardById(rewardId)
    }

    // --- Redemptions ---
    fun getRedemptionsForChildFlow(childId: String): Flow<List<Redemption>> {
        return dao.getRedemptionsForChild(childId)
    }

    fun getRedemptionsForFamilyFlow(familyCode: String): Flow<List<Redemption>> {
        return dao.getRedemptionsForFamily(familyCode)
    }

    suspend fun requestRedemption(childId: String, reward: Reward): Boolean {
        val child = dao.getChildById(childId) ?: return false
        if (child.starBalance < reward.starsCost) return false

        val redemptionId = UUID.randomUUID().toString()
        val redemption = Redemption(
            redemptionId = redemptionId,
            childId = childId,
            familyCode = reward.familyCode,
            rewardId = reward.rewardId,
            rewardNameAr = reward.nameAr,
            rewardNameEn = reward.nameEn,
            rewardEmoji = reward.emoji,
            starsCost = reward.starsCost,
            status = "pending"
        )
        dao.insertRedemption(redemption)

        // Send notification to parent
        val parent = getFamily(reward.familyCode)
        if (parent != null) {
            dao.insertNotification(
                Notification(
                    notifId = UUID.randomUUID().toString(),
                    familyCode = reward.familyCode,
                    type = "reward-pending",
                    titleAr = "طلب استبدال جائزة 🎁",
                    titleEn = "Reward redemption request 🎁",
                    bodyAr = "يريد ${child.firstName} استبدال جائزة: ${reward.nameAr} بـ ${reward.starsCost} نجمة ⭐",
                    bodyEn = "${child.firstName} requested reward: ${reward.nameEn} for ${reward.starsCost} stars",
                    targetUserId = parent.parentEmail,
                    senderUserId = childId,
                    relatedChildId = childId
                )
            )
        }
        return true
    }

    suspend fun resolveRedemption(redemptionId: String, status: String, parentId: String, reason: String? = null) {
        val redemption = dao.getRedemptionById(redemptionId) ?: return
        if (redemption.status != "pending") return

        val updated = redemption.copy(
            status = status,
            rejectionReason = reason,
            resolvedAt = System.currentTimeMillis()
        )
        dao.updateRedemption(updated)

        val child = dao.getChildById(redemption.childId) ?: return

        if (status == "approved") {
            // Deduct stars
            val newBalance = (child.starBalance - redemption.starsCost).coerceAtLeast(0)
            dao.updateChild(child.copy(starBalance = newBalance))

            dao.insertNotification(
                Notification(
                    notifId = UUID.randomUUID().toString(),
                    familyCode = redemption.familyCode,
                    type = "reward-approved",
                    titleAr = "ألف مبروك مكافأتك! 🎉",
                    titleEn = "Congratulations on your reward! 🎉",
                    bodyAr = "تمت الموافقة على استبدال مكافأة ${redemption.rewardNameAr}! نرجو لك دوام المتعة والفائدة!",
                    bodyEn = "Your reward ${redemption.rewardNameEn} was approved!",
                    targetUserId = child.childId,
                    senderUserId = parentId,
                    relatedChildId = child.childId
                )
            )
        } else {
            // Rejected
            dao.insertNotification(
                Notification(
                    notifId = UUID.randomUUID().toString(),
                    familyCode = redemption.familyCode,
                    type = "reward-rejected",
                    titleAr = "لم نتمكن من الموافقة 📝",
                    titleEn = "Redemption request denied 📝",
                    bodyAr = "تم رفض طلب استبدال مكافأة ${redemption.rewardNameAr}. السبب: $reason",
                    bodyEn = "Your reward request was rejected. Reason: $reason",
                    targetUserId = child.childId,
                    senderUserId = parentId,
                    relatedChildId = child.childId
                )
            )
        }
    }

    // --- Messages Inbox ---
    fun getMessagesForChildFlow(childId: String): Flow<List<EncouragingMessage>> {
        return dao.getMessagesForChild(childId)
    }

    suspend fun sendEncouragingMessage(childId: String, content: String, senderName: String) {
        val message = EncouragingMessage(
            messageId = UUID.randomUUID().toString(),
            childId = childId,
            content = content,
            senderName = senderName
        )
        dao.insertMessage(message)

        dao.insertNotification(
            Notification(
                notifId = UUID.randomUUID().toString(),
                familyCode = getCurrentFamilyCode().orEmpty(),
                type = "encouraging-message",
                titleAr = "رسالة تشجيعية خاصة لحبيبنا 💌",
                titleEn = "New encouraging message 💌",
                bodyAr = "أرسل إليك $senderName رسالة تشجيعية دافئة ومحفزة!",
                bodyEn = "$senderName sent you an encouraging message!",
                targetUserId = childId,
                senderUserId = "parent"
            )
        )
    }

    suspend fun markAllMessagesAsRead(childId: String) {
        dao.markAllMessagesAsRead(childId)
    }

    // --- Notifications ---
    fun getNotificationsFlow(userId: String): Flow<List<Notification>> {
        return dao.getNotificationsForUser(userId)
    }

    suspend fun markNotificationsRead(userId: String) {
        dao.markAllNotificationsAsRead(userId)
    }

    // --- Badges System Automatic Checker ---
    private suspend fun checkAndAwardBadges(childId: String) {
        val child = dao.getChildById(childId) ?: return
        val currentBadges = child.badges.split(",").filter { it.isNotEmpty() }.toMutableSet()

        val allBadges = NujoomConstants.ACHIEVEMENT_BADGES
        var countAwarded = 0

        for (badge in allBadges) {
            if (currentBadges.contains(badge.id)) continue

            var triggerMet = false
            when (badge.triggerType) {
                "streak" -> {
                    if (child.streak >= badge.triggerValue) {
                        triggerMet = true
                    }
                }
                "total-stars" -> {
                    if (child.totalStarsEarned >= badge.triggerValue) {
                        triggerMet = true
                    }
                }
                "prayer-streak" -> {
                    // Check history for continuous completed prayers (approximation based on recorded counts)
                    val history = dao.getPrayerHistory(childId).firstOrNull() ?: emptyList()
                    val totalDone = history.count { it.fajr && it.dhuhr && it.asr && it.maghrib && it.isha }
                    if (totalDone >= badge.triggerValue) {
                        triggerMet = true
                    }
                }
                "task-count" -> {
                    // Check completed tasks of a specific description/emoji or category
                    val today = getTodayKey()
                    val tasks = dao.getTasksForChildSync(childId, today)
                    val completedCount = tasks.count {
                        it.status == "completed" && (it.isDefault && badge.relatedId != null && it.nameAr.contains("ر") || it.emoji == "📚" || it.emoji == "🛏️")
                    }
                    // Since it's daily, we can do mock matching or award if any historical completions happen
                    if (child.totalStarsEarned > (badge.triggerValue * 5)) {
                        triggerMet = true
                    }
                }
                "recitation-count" -> {
                    val count = dao.getRecitationsForChildSync(childId).size
                    if (count >= badge.triggerValue) {
                        triggerMet = true
                    }
                }
            }

            if (triggerMet) {
                currentBadges.add(badge.id)
                countAwarded++

                // Notify child
                dao.insertNotification(
                    Notification(
                        notifId = UUID.randomUUID().toString(),
                        familyCode = child.familyCode,
                        type = "badge-earned",
                        titleAr = "شارة تميز جديدة! 🏅",
                        titleEn = "New badge earned! 🏅",
                        bodyAr = "رائع! لقد حصلت على شارة: ${badge.nameAr}! ${badge.descriptionAr}",
                        bodyEn = "Congratulations! You earned the badge: ${badge.nameEn}!",
                        targetUserId = childId,
                        senderUserId = "system"
                    )
                )
            }
        }

        if (countAwarded > 0) {
            val updated = child.copy(
                badges = currentBadges.joinToString(",")
            )
            dao.updateChild(updated)
        }
    }
}
