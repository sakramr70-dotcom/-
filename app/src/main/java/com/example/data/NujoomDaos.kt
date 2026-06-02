package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface NujoomDao {

    // --- Family ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFamily(family: Family)

    @Query("SELECT * FROM family WHERE familyCode = :familyCode LIMIT 1")
    suspend fun getFamilyByCode(familyCode: String): Family?

    @Query("SELECT * FROM family WHERE parentEmail = :email LIMIT 1")
    suspend fun getFamilyByEmail(email: String): Family?

    @Update
    suspend fun updateFamily(family: Family)

    // --- Child ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChild(child: Child)

    @Query("SELECT * FROM child WHERE childId = :childId LIMIT 1")
    suspend fun getChildById(childId: String): Child?

    @Query("SELECT * FROM child WHERE familyCode = :familyCode")
    fun getChildrenByFamily(familyCode: String): Flow<List<Child>>

    @Query("SELECT * FROM child WHERE familyCode = :familyCode")
    suspend fun getChildrenByFamilySync(familyCode: String): List<Child>

    @Update
    suspend fun updateChild(child: Child)

    @Query("SELECT * FROM child WHERE childId = :childId LIMIT 1")
    fun getChildByIdFlow(childId: String): Flow<Child?>

    // --- Tasks ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>)

    @Query("SELECT * FROM task WHERE childId = :childId AND dateKey = :dateKey ORDER BY `order` ASC")
    fun getTasksForChild(childId: String, dateKey: String): Flow<List<Task>>

    @Query("SELECT * FROM task WHERE childId = :childId AND dateKey = :dateKey ORDER BY `order` ASC")
    suspend fun getTasksForChildSync(childId: String, dateKey: String): List<Task>

    @Query("SELECT * FROM task WHERE familyCode = :familyCode AND dateKey = :dateKey")
    fun getRecentTasksByFamily(familyCode: String, dateKey: String): Flow<List<Task>>

    @Update
    suspend fun updateTask(task: Task)

    @Query("DELETE FROM task WHERE taskId = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("SELECT * FROM task WHERE taskId = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: String): Task?

    // --- Prayers ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrayer(prayer: Prayer)

    @Query("SELECT * FROM prayer WHERE childId = :childId AND dateKey = :dateKey LIMIT 1")
    fun getPrayerForChild(childId: String, dateKey: String): Flow<Prayer?>

    @Query("SELECT * FROM prayer WHERE childId = :childId AND dateKey = :dateKey LIMIT 1")
    suspend fun getPrayerForChildSync(childId: String, dateKey: String): Prayer?

    @Query("SELECT * FROM prayer WHERE childId = :childId ORDER BY dateKey DESC")
    fun getPrayerHistory(childId: String): Flow<List<Prayer>>

    // --- Recitations ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecitation(recitation: Recitation)

    @Query("SELECT * FROM recitation WHERE childId = :childId ORDER BY recordedAt DESC")
    fun getRecitationsForChild(childId: String): Flow<List<Recitation>>

    @Query("SELECT * FROM recitation WHERE childId = :childId ORDER BY recordedAt DESC")
    suspend fun getRecitationsForChildSync(childId: String): List<Recitation>

    @Query("SELECT * FROM recitation r INNER JOIN child c ON r.childId = c.childId WHERE c.familyCode = :familyCode ORDER BY r.recordedAt DESC")
    fun getRecitationsForFamily(familyCode: String): Flow<List<Recitation>>

    @Update
    suspend fun updateRecitation(recitation: Recitation)

    @Query("SELECT * FROM recitation WHERE recitationId = :id LIMIT 1")
    suspend fun getRecitationById(id: String): Recitation?

    // --- Rewards ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReward(reward: Reward)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewards(rewards: List<Reward>)

    @Query("SELECT * FROM reward WHERE familyCode = :familyCode ORDER BY starsCost ASC")
    fun getRewardsByFamily(familyCode: String): Flow<List<Reward>>

    @Query("SELECT * FROM reward WHERE familyCode = :familyCode ORDER BY starsCost ASC")
    suspend fun getRewardsByFamilySync(familyCode: String): List<Reward>

    @Update
    suspend fun updateReward(reward: Reward)

    @Query("DELETE FROM reward WHERE rewardId = :rewardId")
    suspend fun deleteRewardById(rewardId: String)

    @Query("SELECT * FROM reward WHERE rewardId = :rewardId LIMIT 1")
    suspend fun getRewardById(rewardId: String): Reward?

    // --- Redemptions ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRedemption(redemption: Redemption)

    @Query("SELECT * FROM redemption WHERE childId = :childId ORDER BY requestedAt DESC")
    fun getRedemptionsForChild(childId: String): Flow<List<Redemption>>

    @Query("SELECT * FROM redemption WHERE familyCode = :familyCode ORDER BY requestedAt DESC")
    fun getRedemptionsForFamily(familyCode: String): Flow<List<Redemption>>

    @Update
    suspend fun updateRedemption(redemption: Redemption)

    @Query("SELECT * FROM redemption WHERE redemptionId = :redemptionId LIMIT 1")
    suspend fun getRedemptionById(redemptionId: String): Redemption?

    // --- Notifications ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: Notification)

    @Query("SELECT * FROM notification WHERE targetUserId = :userId ORDER BY createdAt DESC")
    fun getNotificationsForUser(userId: String): Flow<List<Notification>>

    @Query("UPDATE notification SET isRead = 1 WHERE targetUserId = :userId")
    suspend fun markAllNotificationsAsRead(userId: String)

    @Query("DELETE FROM notification WHERE notifId = :notifId")
    suspend fun deleteNotification(notifId: String)

    // --- Encouraging Messages ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: EncouragingMessage)

    @Query("SELECT * FROM encouraging_message WHERE childId = :childId ORDER BY createdAt DESC")
    fun getMessagesForChild(childId: String): Flow<List<EncouragingMessage>>

    @Query("UPDATE encouraging_message SET isRead = 1 WHERE childId = :childId")
    suspend fun markAllMessagesAsRead(childId: String)
}
