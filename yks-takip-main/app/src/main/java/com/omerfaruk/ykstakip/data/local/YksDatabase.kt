package com.omerfaruk.ykstakip.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Index
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Entity(
    tableName = "topics",
    indices = [Index(value = ["subjectName", "title", "category"], unique = true)]
)
data class TopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectName: String,
    val title: String,
    val isCompleted: Boolean = false,
    val category: String // TYT, AYT_SAY, AYT_EA, AYT_SOZ
)

@Entity(tableName = "net_results")
data class NetResultEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long,
    val type: String, // TYT, AYT, BRANS
    val totalNet: Float,
    val details: String
)

@Entity(tableName = "wrong_questions")
data class WrongQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val subjectName: String,
    val examType: String = "TYT",  // "TYT" veya "AYT"
    val topicTitle: String,
    val imagePath: String,
    val isSolved: Boolean = false,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "study_times")
data class StudyTimeEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long = System.currentTimeMillis(),
    val subjectName: String,
    val topicTitle: String,
    val durationSeconds: Long
)

@Entity(tableName = "snippet_interactions")
data class SnippetInteractionEntity(
    @PrimaryKey val snippetId: String,
    val topicId: String,
    val subjectName: String,
    val isLiked: Boolean = false,
    val isKnown: Boolean = false,
    val isSaved: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis()
)

@Dao
interface YksDao {
    @Query("SELECT * FROM topics")
    fun getAllTopics(): Flow<List<TopicEntity>>

    @Query("SELECT * FROM topics WHERE subjectName = :subjectName")
    fun getTopicsBySubject(subjectName: String): Flow<List<TopicEntity>>

    @Update
    suspend fun updateTopic(topic: TopicEntity): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTopics(topics: List<TopicEntity>): List<Long>

    @Query("DELETE FROM topics")
    suspend fun deleteAllTopics(): Int

    @Query("UPDATE topics SET isCompleted = 0")
    suspend fun resetAllTopicsProgress(): Int

    @Query("SELECT COUNT(*) FROM topics")
    suspend fun getTopicsCount(): Int

    @Insert
    suspend fun insertNetResult(netResult: NetResultEntity): Long

    @Query("SELECT * FROM net_results WHERE type = :type ORDER BY date ASC")
    fun getNetResultsByType(type: String): Flow<List<NetResultEntity>>

    @Query("SELECT * FROM net_results")
    fun getAllNetResults(): Flow<List<NetResultEntity>>

    @Update
    suspend fun updateNetResult(netResult: NetResultEntity): Int

    @Query("DELETE FROM net_results WHERE id = :id")
    suspend fun deleteNetResultById(id: Int): Int

    @Query("DELETE FROM net_results")
    suspend fun deleteAllNetResults(): Int

    // Wrong Questions
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWrongQuestion(question: WrongQuestionEntity): Long

    @Delete
    suspend fun deleteWrongQuestion(question: WrongQuestionEntity): Int

    @Query("SELECT * FROM wrong_questions WHERE subjectName = :subjectName AND examType = :examType ORDER BY addedAt ASC")
    fun getWrongQuestionsBySubjectAndType(subjectName: String, examType: String): Flow<List<WrongQuestionEntity>>

    @Update
    suspend fun updateWrongQuestion(question: WrongQuestionEntity): Int

    @Query("SELECT COUNT(*) FROM wrong_questions WHERE subjectName = :subjectName AND examType = :examType")
    fun getWrongQuestionCountBySubjectAndType(subjectName: String, examType: String): Flow<Int>

    // Study Times
    @Insert
    suspend fun insertStudyTime(studyTime: StudyTimeEntity): Long

    @Query("SELECT * FROM study_times ORDER BY date DESC")
    fun getAllStudyTimes(): Flow<List<StudyTimeEntity>>

    // Calculation History
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCalculationHistory(history: CalculationHistoryEntity): Long

    @Query("SELECT * FROM calculation_history ORDER BY date DESC")
    fun getAllCalculationHistory(): Flow<List<CalculationHistoryEntity>>

    @Query("DELETE FROM calculation_history WHERE id = :id")
    suspend fun deleteCalculationHistoryById(id: Int): Int

    @Query("DELETE FROM calculation_history")
    suspend fun deleteAllCalculationHistory(): Int

    // Snippet Interactions
    @Query("SELECT * FROM snippet_interactions")
    fun getAllSnippetInteractions(): Flow<List<SnippetInteractionEntity>>

    @Query("SELECT * FROM snippet_interactions WHERE snippetId = :snippetId LIMIT 1")
    suspend fun getSnippetInteraction(snippetId: String): SnippetInteractionEntity?

    @Query("SELECT * FROM snippet_interactions WHERE isSaved = 1")
    fun getSavedSnippetInteractions(): Flow<List<SnippetInteractionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateInteraction(interaction: SnippetInteractionEntity): Long

    // Followed Subjects
    @Query("SELECT * FROM followed_subjects")
    fun getAllFollowedSubjects(): Flow<List<FollowedSubjectEntity>>

    @Query("SELECT * FROM followed_subjects WHERE subjectName = :subjectName LIMIT 1")
    suspend fun getFollowedSubject(subjectName: String): FollowedSubjectEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateFollowedSubject(followedSubject: FollowedSubjectEntity): Long

    // Question Logs
    @Query("SELECT * FROM question_logs")
    fun getAllQuestionLogs(): Flow<List<QuestionLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateQuestionLog(log: QuestionLogEntity): Long

    @Delete
    suspend fun deleteQuestionLog(log: QuestionLogEntity): Int

    @Query("DELETE FROM question_logs")
    suspend fun deleteAllQuestionLogs(): Int

    @Query("DELETE FROM wrong_questions")
    suspend fun deleteAllWrongQuestions(): Int

    @Query("DELETE FROM study_times")
    suspend fun deleteAllStudyTimes(): Int

    @Query("DELETE FROM snippet_interactions")
    suspend fun deleteAllSnippetInteractions(): Int

    @Query("DELETE FROM followed_subjects")
    suspend fun deleteAllFollowedSubjects(): Int
}

@Entity(tableName = "followed_subjects")
data class FollowedSubjectEntity(
    @PrimaryKey val subjectName: String,
    val isFollowed: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "question_logs")
data class QuestionLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val date: Long = System.currentTimeMillis(),
    val subjectName: String,
    val topicTitle: String,
    val correctCount: Int,
    val wrongCount: Int,
    val updatedAt: Long = System.currentTimeMillis()
)

@Database(
    entities = [
        TopicEntity::class,
        NetResultEntity::class,
        WrongQuestionEntity::class,
        StudyTimeEntity::class,
        CalculationHistoryEntity::class,
        SnippetInteractionEntity::class,
        FollowedSubjectEntity::class,
        QuestionLogEntity::class
    ],
    version = 18
)
abstract class YksDatabase : RoomDatabase() {
    abstract fun yksDao(): YksDao
}
