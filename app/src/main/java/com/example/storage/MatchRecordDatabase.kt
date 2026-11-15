package com.example.storage

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "match_records")
data class MatchRecordEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0,
  val playerFighter: String,
  val opponentFighter: String,
  val gameMode: String,
  val won: Boolean,
  val maxCombo: Int,
  val damageDealt: Int,
  val ultimatesLanded: Int,
  val remainingSeconds: Int,
  val timestampMs: Long = System.currentTimeMillis(),
)

@Dao
interface MatchRecordDao {
  @Query("SELECT * FROM match_records ORDER BY timestampMs DESC LIMIT 50")
  fun getRecentMatches(): Flow<List<MatchRecordEntity>>

  @Query("SELECT COUNT(*) FROM match_records WHERE won = 1")
  fun getTotalWins(): Flow<Int>

  @Query("SELECT COALESCE(MAX(maxCombo), 0) FROM match_records")
  fun getBestCombo(): Flow<Int>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertMatch(record: MatchRecordEntity)

  @Query("DELETE FROM match_records")
  suspend fun clearAll()
}

@Database(entities = [MatchRecordEntity::class], version = 1, exportSchema = false)
abstract class MatchRecordDatabase : RoomDatabase() {
  abstract fun matchRecordDao(): MatchRecordDao

  companion object {
    @Volatile private var INSTANCE: MatchRecordDatabase? = null

    fun getInstance(context: Context): MatchRecordDatabase =
      INSTANCE
        ?: synchronized(this) {
          INSTANCE
            ?: Room.databaseBuilder(
                context.applicationContext,
                MatchRecordDatabase::class.java,
                "anime_arena_records.db",
              )
              .build()
              .also { INSTANCE = it }
        }
  }
}

class MatchRecordRepository(private val dao: MatchRecordDao) {
  val recentMatches: Flow<List<MatchRecordEntity>> = dao.getRecentMatches()
  val totalWins: Flow<Int> = dao.getTotalWins()
  val bestCombo: Flow<Int> = dao.getBestCombo()

  suspend fun recordMatch(record: MatchRecordEntity) = dao.insertMatch(record)

  suspend fun clearHistory() = dao.clearAll()
}
