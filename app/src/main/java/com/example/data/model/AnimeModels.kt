package com.example.data.model

enum class WatchStatus(val labelAr: String, val labelEn: String) {
    WATCHING("أشاهده حالياً", "Watching"),
    COMPLETED("مكتمل", "Completed"),
    PLAN_TO_WATCH("أخطط لمشاهدته", "Plan to Watch"),
    ON_HOLD("متوقف مؤقتاً", "On Hold"),
    FAVORITE("المفضلة", "Favorites")
}

enum class AnimeDayOfWeek(val key: String, val labelAr: String, val labelEn: String) {
    SATURDAY("saturday", "السبت", "Saturday"),
    SUNDAY("sunday", "الأحد", "Sunday"),
    MONDAY("monday", "الإثنين", "Monday"),
    TUESDAY("tuesday", "الثلاثاء", "Tuesday"),
    WEDNESDAY("wednesday", "الأربعاء", "Wednesday"),
    THURSDAY("thursday", "الخميس", "Thursday"),
    FRIDAY("friday", "الجمعة", "Friday")
}

data class AnimeCharacter(
    val id: Int,
    val name: String,
    val roleAr: String,
    val roleEn: String,
    val voiceActor: String,
    val imageUrl: String
)

data class RelatedAnime(
    val malId: Int,
    val title: String,
    val titleAr: String,
    val relationAr: String,
    val relationEn: String,
    val posterUrl: String
)

data class AnimeUserStats(
    val watchingCount: Int,
    val completedCount: Int,
    val planToWatchCount: Int,
    val droppedCount: Int,
    val score10Percent: Float,
    val score9Percent: Float,
    val score8Percent: Float,
    val score7Percent: Float
)

data class AnimeEpisode(
    val malId: Int,
    val number: Int,
    val titleEn: String,
    val titleAr: String,
    val airedDate: String,
    val durationMinutes: Int = 24,
    val isFiller: Boolean = false,
    val rating: Double = 4.8,
    val directStreamUrl: String = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
)

data class StreamServer(
    val id: String,
    val name: String,
    val badge: String,
    val latencyMs: Int,
    val isDirectStream: Boolean,
    val qualities: List<String> = listOf("1080p FHD", "720p HD", "480p SD"),
    val streamUrl: String
)

data class AnimeItem(
    val malId: Int,
    val titleEn: String,
    val titleRomaji: String,
    val titleAr: String,
    val synopsisEn: String,
    val synopsisAr: String,
    val posterUrl: String,
    val bannerUrl: String,
    val score: Double,
    val scoredBy: Int,
    val rank: Int,
    val popularity: Int,
    val statusEn: String, // Airing, Completed, Upcoming
    val statusAr: String, // مستمر، مكتمل، قريباً
    val type: String, // TV, Movie, OVA, ONA, Special
    val episodesCount: Int,
    val latestEpisodeNumber: Int,
    val releaseTimeTextAr: String,
    val releaseTimeTextEn: String,
    val season: String, // Winter, Spring, Summer, Fall
    val year: Int,
    val studio: String,
    val ageRating: String,
    val genresEn: List<String>,
    val genresAr: List<String>,
    val broadcastDay: AnimeDayOfWeek,
    val broadcastTimeJst: String,
    val countdownMinutes: Int,
    val trailerUrl: String,
    val sourceDomain: String? = null,
    val characters: List<AnimeCharacter> = emptyList(),
    val relatedAnime: List<RelatedAnime> = emptyList(),
    val userStats: AnimeUserStats = AnimeUserStats(
        watchingCount = 148200,
        completedCount = 312500,
        planToWatchCount = 94100,
        droppedCount = 8400,
        score10Percent = 0.42f,
        score9Percent = 0.31f,
        score8Percent = 0.18f,
        score7Percent = 0.09f
    )
)

data class ScraperStepLog(
    val stepIndex: Int,
    val titleAr: String,
    val titleEn: String,
    val detail: String,
    val isCompleted: Boolean,
    val isActive: Boolean,
    val durationMs: Int
)

data class ScrapedSourceCatalog(
    val sourceId: Int,
    val sourceName: String,
    val sourceUrl: String,
    val domain: String,
    val extractedItems: List<AnimeItem>,
    val adsBlockedCount: Int,
    val scriptsStrippedCount: Int,
    val responseTimeMs: Int,
    val usedLiveProxy: Boolean
)
