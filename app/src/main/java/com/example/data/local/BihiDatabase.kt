package com.example.data.local

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

@Entity(tableName = "watchlist")
data class WatchlistEntity(
    @PrimaryKey val malId: Int,
    val titleAr: String,
    val titleEn: String,
    val posterUrl: String,
    val score: Double,
    val statusCategory: String, // WATCHING, COMPLETED, PLAN_TO_WATCH, ON_HOLD, FAVORITE
    val episodesCount: Int,
    val studio: String,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "watch_history", primaryKeys = ["malId", "episodeNumber"])
data class WatchHistoryEntity(
    val malId: Int,
    val episodeNumber: Int,
    val animeTitleAr: String,
    val animeTitleEn: String,
    val episodeTitleAr: String,
    val episodeTitleEn: String,
    val posterUrl: String,
    val serverName: String,
    val quality: String,
    val positionSeconds: Int,
    val durationSeconds: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "episode_states", primaryKeys = ["malId", "episodeNumber"])
data class EpisodeStateEntity(
    val malId: Int,
    val episodeNumber: Int,
    val animeTitleAr: String,
    val animeTitleEn: String,
    val posterUrl: String,
    val isWatched: Boolean = false,
    val isDownloaded: Boolean = false,
    val downloadQuality: String = "1080p FHD",
    val downloadSizeMb: Int = 340,
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "scraper_sources")
data class ScraperSourceEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val url: String,
    val domain: String,
    val cardSelector: String,
    val titleSelector: String,
    val posterSelector: String,
    val episodeSelector: String,
    val iframeSelector: String,
    val itemCount: Int,
    val adsStripped: Int,
    val statusText: String,
    val isPreloaded: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "community_reviews")
data class CommunityReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val malId: Int,
    val authorName: String,
    val rating: Int,
    val comment: String,
    val isSpoiler: Boolean,
    val likesCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Dao
interface BihiDao {
    // Watchlist
    @Query("SELECT * FROM watchlist ORDER BY updatedAt DESC")
    fun getAllWatchlist(): Flow<List<WatchlistEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchlist(entity: WatchlistEntity)

    @Query("DELETE FROM watchlist WHERE malId = :malId")
    suspend fun removeFromWatchlist(malId: Int)

    // Watch History
    @Query("SELECT * FROM watch_history ORDER BY timestamp DESC")
    fun getAllWatchHistory(): Flow<List<WatchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertWatchHistory(entity: WatchHistoryEntity)

    @Query("DELETE FROM watch_history")
    suspend fun clearWatchHistory()

    // Episode States (Watched & Downloaded)
    @Query("SELECT * FROM episode_states")
    fun getAllEpisodeStates(): Flow<List<EpisodeStateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertEpisodeState(entity: EpisodeStateEntity)

    // Scraper Sources
    @Query("SELECT * FROM scraper_sources ORDER BY isPreloaded DESC, createdAt DESC")
    fun getAllScraperSources(): Flow<List<ScraperSourceEntity>>

    @Query("SELECT COUNT(*) FROM scraper_sources")
    suspend fun getScraperSourceCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScraperSource(entity: ScraperSourceEntity): Long

    @Query("DELETE FROM scraper_sources WHERE id = :id")
    suspend fun deleteScraperSource(id: Int)

    // Community Reviews
    @Query("SELECT * FROM community_reviews ORDER BY createdAt DESC")
    fun getAllReviews(): Flow<List<CommunityReviewEntity>>

    @Query("SELECT COUNT(*) FROM community_reviews")
    suspend fun getReviewsCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(entity: CommunityReviewEntity)

    @Query("UPDATE community_reviews SET likesCount = likesCount + 1 WHERE id = :id")
    suspend fun incrementReviewLike(id: Int)
}

@Database(
    entities = [
        WatchlistEntity::class,
        WatchHistoryEntity::class,
        EpisodeStateEntity::class,
        ScraperSourceEntity::class,
        CommunityReviewEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class BihiDatabase : RoomDatabase() {
    abstract fun bihiDao(): BihiDao

    companion object {
        @Volatile
        private var INSTANCE: BihiDatabase? = null

        fun getInstance(context: Context): BihiDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    BihiDatabase::class.java,
                    "anime_bihi_db"
                ).fallbackToDestructiveMigration(false).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
