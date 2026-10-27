package com.example.data.repository

import com.example.data.local.BihiDao
import com.example.data.local.CommunityReviewEntity
import com.example.data.local.EpisodeStateEntity
import com.example.data.local.ScraperSourceEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.local.WatchlistEntity
import com.example.data.model.AnimeEpisode
import com.example.data.model.AnimeItem
import com.example.data.model.ScrapedSourceCatalog
import com.example.data.model.ScraperStepLog
import com.example.data.model.StreamServer
import com.example.data.model.WatchStatus
import com.example.data.remote.JikanService
import com.example.data.scraper.BihiWebScraperEngine
import kotlinx.coroutines.flow.Flow

class BihiRepository(
    private val dao: BihiDao,
    private val jikanService: JikanService = JikanService(),
    private val scraperEngine: BihiWebScraperEngine = BihiWebScraperEngine(jikanService)
) {
    val watchlistFlow: Flow<List<WatchlistEntity>> = dao.getAllWatchlist()
    val watchHistoryFlow: Flow<List<WatchHistoryEntity>> = dao.getAllWatchHistory()
    val episodeStatesFlow: Flow<List<EpisodeStateEntity>> = dao.getAllEpisodeStates()
    val scraperSourcesFlow: Flow<List<ScraperSourceEntity>> = dao.getAllScraperSources()
    val communityReviewsFlow: Flow<List<CommunityReviewEntity>> = dao.getAllReviews()

    suspend fun ensureInitialSeedData() {
        if (dao.getScraperSourceCount() == 0) {
            scraperEngine.getPreloadedScraperProfiles().forEach { profile ->
                dao.insertScraperSource(profile)
            }
        }
        if (dao.getReviewsCount() == 0) {
            val initialReviews = listOf(
                CommunityReviewEntity(
                    malId = 52299,
                    authorName = "Kaito_Slayer",
                    rating = 5,
                    comment = "التحريك في قتال إيغريس وسونغ جين-وو أسطوري بكل معنى الكلمة! سيرفر Bihi Native شغال بدون أي تقطيع.",
                    isSpoiler = false,
                    likesCount = 48
                ),
                CommunityReviewEntity(
                    malId = 52299,
                    authorName = "ShadowMonarch_AR",
                    rating = 5,
                    comment = "حرق من المانهوا: اللحظة التي يقول فيها Arise ويستدعي جيش الظلال بالكامل في الأرك القادم ستكسر الإنترنت!",
                    isSpoiler = true,
                    likesCount = 31
                ),
                CommunityReviewEntity(
                    malId = 52991,
                    authorName = "ElvenMage",
                    rating = 5,
                    comment = "فريرين عمل فني خالص، الموسيقى التصويرية والإخراج من استوديو مادهاوس يجعلك تعيش كل لحظة.",
                    isSpoiler = false,
                    likesCount = 63
                ),
                CommunityReviewEntity(
                    malId = 51009,
                    authorName = "GojoSatoru_99",
                    rating = 5,
                    comment = "تحذير حرق شيبويا: ختم غوجو ساتورو داخل بوابة السجن قلب موازين القوى تماماً في عالم جوجوتسو!",
                    isSpoiler = true,
                    likesCount = 54
                )
            )
            initialReviews.forEach { dao.insertReview(it) }
        }
    }

    suspend fun fetchHomeCatalog(): List<AnimeItem> = jikanService.fetchHomeCatalog()

    suspend fun fetchSeasonalCatalog(): List<AnimeItem> = jikanService.fetchSeasonalCatalog()

    fun getCuratedCatalog(): List<AnimeItem> = jikanService.getRichCuratedCatalog()

    suspend fun searchAnime(query: String): List<AnimeItem> = jikanService.searchAnimeRemote(query)

    suspend fun fetchEpisodes(anime: AnimeItem): List<AnimeEpisode> =
        jikanService.fetchEpisodesForAnime(anime)

    fun getServersForEpisode(anime: AnimeItem, episode: AnimeEpisode): List<StreamServer> =
        jikanService.getServersForEpisode(anime, episode)

    suspend fun setWatchlistStatus(anime: AnimeItem, status: WatchStatus?) {
        if (status == null) {
            dao.removeFromWatchlist(anime.malId)
        } else {
            dao.upsertWatchlist(
                WatchlistEntity(
                    malId = anime.malId,
                    titleAr = anime.titleAr,
                    titleEn = anime.titleEn,
                    posterUrl = anime.posterUrl,
                    score = anime.score,
                    statusCategory = status.name,
                    episodesCount = anime.episodesCount,
                    studio = anime.studio
                )
            )
        }
    }

    suspend fun recordWatchHistory(
        anime: AnimeItem,
        episode: AnimeEpisode,
        serverName: String,
        quality: String,
        positionSeconds: Int,
        durationSeconds: Int
    ) {
        dao.upsertWatchHistory(
            WatchHistoryEntity(
                malId = anime.malId,
                episodeNumber = episode.number,
                animeTitleAr = anime.titleAr,
                animeTitleEn = anime.titleEn,
                episodeTitleAr = episode.titleAr,
                episodeTitleEn = episode.titleEn,
                posterUrl = anime.posterUrl,
                serverName = serverName,
                quality = quality,
                positionSeconds = positionSeconds,
                durationSeconds = durationSeconds
            )
        )
    }

    suspend fun clearHistory() {
        dao.clearWatchHistory()
    }

    suspend fun toggleEpisodeWatched(
        anime: AnimeItem,
        episodeNumber: Int,
        currentlyWatched: Boolean,
        existingState: EpisodeStateEntity?
    ) {
        dao.upsertEpisodeState(
            EpisodeStateEntity(
                malId = anime.malId,
                episodeNumber = episodeNumber,
                animeTitleAr = anime.titleAr,
                animeTitleEn = anime.titleEn,
                posterUrl = anime.posterUrl,
                isWatched = !currentlyWatched,
                isDownloaded = existingState?.isDownloaded ?: false,
                downloadQuality = existingState?.downloadQuality ?: "1080p FHD",
                downloadSizeMb = existingState?.downloadSizeMb ?: 340
            )
        )
    }

    suspend fun markEpisodeDownloaded(
        anime: AnimeItem,
        episodeNumber: Int,
        quality: String,
        sizeMb: Int,
        existingState: EpisodeStateEntity?
    ) {
        dao.upsertEpisodeState(
            EpisodeStateEntity(
                malId = anime.malId,
                episodeNumber = episodeNumber,
                animeTitleAr = anime.titleAr,
                animeTitleEn = anime.titleEn,
                posterUrl = anime.posterUrl,
                isWatched = existingState?.isWatched ?: true,
                isDownloaded = true,
                downloadQuality = quality,
                downloadSizeMb = sizeMb
            )
        )
    }

    suspend fun runScraper(
        rawUrl: String,
        cardSelector: String,
        titleSelector: String,
        posterSelector: String,
        episodeSelector: String,
        iframeSelector: String,
        baseCatalog: List<AnimeItem>,
        saveSourceToDb: Boolean,
        onStepUpdate: (List<ScraperStepLog>, Float) -> Unit
    ): ScrapedSourceCatalog {
        val result = scraperEngine.runInteractiveScrape(
            rawUrl = rawUrl,
            customCardSelector = cardSelector,
            customTitleSelector = titleSelector,
            customPosterSelector = posterSelector,
            customEpisodeSelector = episodeSelector,
            customIframeSelector = iframeSelector,
            baseCatalog = baseCatalog,
            onStepUpdate = onStepUpdate
        )
        if (saveSourceToDb) {
            dao.insertScraperSource(
                ScraperSourceEntity(
                    name = result.sourceName,
                    url = result.sourceUrl,
                    domain = result.domain,
                    cardSelector = cardSelector,
                    titleSelector = titleSelector,
                    posterSelector = posterSelector,
                    episodeSelector = episodeSelector,
                    iframeSelector = iframeSelector,
                    itemCount = result.extractedItems.size,
                    adsStripped = result.adsBlockedCount,
                    statusText = "متصل • ${result.responseTimeMs}ms",
                    isPreloaded = false
                )
            )
        }
        return result
    }

    fun getCatalogForSavedSource(source: ScraperSourceEntity, baseCatalog: List<AnimeItem>): ScrapedSourceCatalog {
        val items = scraperEngine.buildDomainMappedCatalog(source.domain, baseCatalog)
        return ScrapedSourceCatalog(
            sourceId = source.id,
            sourceName = source.name,
            sourceUrl = source.url,
            domain = source.domain,
            extractedItems = items,
            adsBlockedCount = source.adsStripped,
            scriptsStrippedCount = 12,
            responseTimeMs = 190,
            usedLiveProxy = true
        )
    }

    suspend fun deleteScraperSource(id: Int) {
        dao.deleteScraperSource(id)
    }

    suspend fun addCommunityReview(
        malId: Int,
        authorName: String,
        rating: Int,
        comment: String,
        isSpoiler: Boolean
    ) {
        dao.insertReview(
            CommunityReviewEntity(
                malId = malId,
                authorName = authorName.ifBlank { "عاشق الأنمي" },
                rating = rating,
                comment = comment,
                isSpoiler = isSpoiler,
                likesCount = 1
            )
        )
    }

    suspend fun likeReview(reviewId: Int) {
        dao.incrementReviewLike(reviewId)
    }
}
