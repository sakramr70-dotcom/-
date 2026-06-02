package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "family")
data class Family(
    @PrimaryKey val familyCode: String,
    val parentName: String,
    val parentEmail: String,
    val parentPassword: String,
    val language: String = "ar", // "ar" or "en"
    val theme: String = "dark" // "dark" or "light"
)

@Entity(tableName = "child")
data class Child(
    @PrimaryKey val childId: String,
    val familyCode: String,
    val firstName: String,
    val lastName: String,
    val age: Int,
    val avatarId: String,
    val starBalance: Int = 0,
    val totalStarsEarned: Int = 0,
    val streak: Int = 0,
    val lastCompletionDate: String = "", // "YYYY-MM-DD"
    val badges: String = "", // Comma-separated list of badge IDs
    val memorizeSurah: String = "سورة الملك",
    val memorizeAyahs: Int = 5
)

@Entity(tableName = "task")
data class Task(
    @PrimaryKey val taskId: String,
    val childId: String,
    val familyCode: String,
    val nameAr: String,
    val nameEn: String,
    val descriptionAr: String,
    val descriptionEn: String,
    val emoji: String,
    val starsReward: Int,
    val requiresProof: Boolean,
    val frequency: String, // "daily", "weekly", "once"
    val order: Int,
    val isDefault: Boolean,
    val isActive: Boolean = true,
    val status: String, // "incomplete", "in-progress", "awaiting-review", "completed", "rejected"
    val proofPhotoPath: String? = null,
    val proofSubmittedAt: Long? = null,
    val completedAt: Long? = null,
    val approvedBy: String? = null,
    val rejectionReason: String? = null,
    val dateKey: String // "YYYY-MM-DD"
)

@Entity(tableName = "prayer")
data class Prayer(
    @PrimaryKey val prayerId: String, // "childId_dateKey"
    val childId: String,
    val dateKey: String, // "YYYY-MM-DD"
    val fajr: Boolean = false,
    val dhuhr: Boolean = false,
    val asr: Boolean = false,
    val maghrib: Boolean = false,
    val isha: Boolean = false,
    val starsEarned: Int = 0,
    val recordedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recitation")
data class Recitation(
    @PrimaryKey val recitationId: String,
    val childId: String,
    val surahName: String = "سورة الملك",
    val audioPath: String? = null,
    val durationSeconds: Int = 0,
    val recordedAt: Long = System.currentTimeMillis(),
    val sheikhComment: String? = null,
    val starsAwarded: Int = 0,
    val reviewedAt: Long? = null,
    val sheikhName: String? = null
)

@Entity(tableName = "reward")
data class Reward(
    @PrimaryKey val rewardId: String,
    val familyCode: String,
    val nameAr: String,
    val nameEn: String,
    val emoji: String,
    val starsCost: Int,
    val isAvailable: Boolean = true,
    val isDefault: Boolean = false,
    val category: String // "food", "screen", "outing", "toy", "experience", "freedom", "learning", "activity"
)

@Entity(tableName = "redemption")
data class Redemption(
    @PrimaryKey val redemptionId: String,
    val childId: String,
    val familyCode: String,
    val rewardId: String,
    val rewardNameAr: String,
    val rewardNameEn: String,
    val rewardEmoji: String,
    val starsCost: Int,
    val status: String, // "pending", "approved", "rejected"
    val rejectionReason: String? = null,
    val requestedAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
)

@Entity(tableName = "notification")
data class Notification(
    @PrimaryKey val notifId: String,
    val familyCode: String,
    val type: String,
    val titleAr: String,
    val titleEn: String,
    val bodyAr: String,
    val bodyEn: String,
    val targetUserId: String, // can be a parentId or a childId
    val senderUserId: String,
    val relatedChildId: String? = null,
    val relatedTaskId: String? = null,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "encouraging_message")
data class EncouragingMessage(
    @PrimaryKey val messageId: String,
    val childId: String,
    val content: String,
    val senderName: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)
