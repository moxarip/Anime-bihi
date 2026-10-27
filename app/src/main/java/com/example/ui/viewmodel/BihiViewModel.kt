package com.example.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.local.CommunityReviewEntity
import com.example.data.local.EpisodeStateEntity
import com.example.data.local.ScraperSourceEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.local.WatchlistEntity
import com.example.data.model.AnimeDayOfWeek
import com.example.data.model.AnimeEpisode
import com.example.data.model.AnimeItem
import com.example.data.model.ScrapedSourceCatalog
import com.example.data.model.ScraperStepLog
import com.example.data.model.StreamServer
import com.example.data.model.WatchStatus
import com.example.data.repository.BihiRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class MainNavTab(val labelAr: String, val labelEn: String) {
    HOME("الرئيسية", "Home"),
    SCHEDULE("جدول الحلقات", "Schedule"),
    WEB_READER("قارئ المواقع", "Web Reader"),
    LIBRARY("مكتبتي", "My Library"),
    EXPLORE("الاستكشاف", "Explore")
}

enum class SortOption(val labelAr: String, val labelEn: String) {
    RATING("الأعلى تقييماً", "Highest Rated"),
    POPULARITY("الأكثر شعبية", "Most Popular"),
    LATEST("الأحدث إضافة", "Latest Added"),
    ALPHABETICAL("أبجدياً", "Alphabetical")
}

data class ActivePlayerSession(
    val anime: AnimeItem,
    val episode: AnimeEpisode,
    val allEpisodes: List<AnimeEpisode>,
    val servers: List<StreamServer>,
    val selectedServer: StreamServer,
    val selectedQuality: String,
    val playbackSpeed: Float = 1.0f,
    val currentSeconds: Int = 142,
    val totalSeconds: Int = 1440,
    val isPlaying: Boolean = true,
    val isTheaterMode: Boolean = false,
    val isPipMinimized: Boolean = false
)

data class BihiUiState(
    val isArabic: Boolean = true,
    val currentTab: MainNavTab = MainNavTab.HOME,
    val isLoadingHome: Boolean = false,
    val catalog: List<AnimeItem> = emptyList(),
    val seasonalCatalog: List<AnimeItem> = emptyList(),
    val heroIndex: Int = 0,
    val selectedDay: AnimeDayOfWeek = AnimeDayOfWeek.SATURDAY,
    // Explore & Filter State
    val searchQuery: String = "",
    val selectedGenre: String = "الكل",
    val selectedStatus: String = "الكل", // الكل, Airing, Completed, Upcoming
    val selectedType: String = "الكل", // الكل, TV, Movie, OVA, ONA
    val selectedSeason: String = "الكل", // الكل, Winter, Spring, Summer, Fall
    val selectedYear: Int? = null,
    val selectedSort: SortOption = SortOption.RATING,
    val isSearchingRemote: Boolean = false,
    val remoteSearchResults: List<AnimeItem>? = null,
    // Details Screen State
    val selectedAnime: AnimeItem? = null,
    val selectedAnimeEpisodes: List<AnimeEpisode> = emptyList(),
    val isLoadingEpisodes: Boolean = false,
    val showTrailerDialog: Boolean = false,
    // Video Player Modal State
    val activePlayerSession: ActivePlayerSession? = null,
    // Flagship Web-Reader State
    val scraperUrlInput: String = "https://witanime.cyou",
    val customCardSelector: String = "div.anime-card-container",
    val customTitleSelector: String = "div.anime-card-title h3 a",
    val customPosterSelector: String = "img.img-responsive",
    val customEpisodeSelector: String = "div.episodes-card-title a",
    val customIframeSelector: String = "iframe#player, source[src*='.m3u8']",
    val isScraping: Boolean = false,
    val scraperProgress: Float = 0f,
    val scraperLogs: List<ScraperStepLog> = emptyList(),
    val activeScrapedCatalog: ScrapedSourceCatalog? = null,
    val scrapedCatalogSearchQuery: String = "",
    val statusBannerMessage: String? = null
)

class BihiViewModel(
    private val repository: BihiRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        BihiUiState(
            catalog = repository.getCuratedCatalog(),
            seasonalCatalog = repository.getCuratedCatalog().filter { it.statusEn == "Airing" }
        )
    )
    val uiState: StateFlow<BihiUiState> = _uiState.asStateFlow()

    val watchlist: StateFlow<List<WatchlistEntity>> = repository.watchlistFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val watchHistory: StateFlow<List<WatchHistoryEntity>> = repository.watchHistoryFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val episodeStates: StateFlow<List<EpisodeStateEntity>> = repository.episodeStatesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val scraperSources: StateFlow<List<ScraperSourceEntity>> = repository.scraperSourcesFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val communityReviews: StateFlow<List<CommunityReviewEntity>> = repository.communityReviewsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var searchJob: Job? = null

    init {
        viewModelScope.launch {
            repository.ensureInitialSeedData()
            refreshLiveJikanCatalog()
        }
    }

    fun refreshLiveJikanCatalog() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingHome = true) }
            val liveHome = repository.fetchHomeCatalog()
            val liveSeasonal = repository.fetchSeasonalCatalog()
            val mergedCatalog = ( repository.getCuratedCatalog() + liveHome ).distinctBy { it.malId }
            _uiState.update {
                it.copy(
                    isLoadingHome = false,
                    catalog = mergedCatalog,
                    seasonalCatalog = liveSeasonal.ifEmpty { mergedCatalog.filter { a -> a.statusEn == "Airing" } }
                )
            }
        }
    }

    fun toggleLanguage() {
        _uiState.update { it.copy(isArabic = !it.isArabic) }
    }

    fun selectTab(tab: MainNavTab) {
        _uiState.update { it.copy(currentTab = tab) }
    }

    fun setHeroIndex(index: Int) {
        _uiState.update { state ->
            val max = state.catalog.take(5).size.coerceAtLeast(1)
            state.copy(heroIndex = index.mod(max))
        }
    }

    fun selectScheduleDay(day: AnimeDayOfWeek) {
        _uiState.update { it.copy(selectedDay = day) }
    }

    // Explore & Filter Actions
    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        searchJob?.cancel()
        if (query.trim().length >= 3) {
            searchJob = viewModelScope.launch {
                delay(350) // Debounce
                _uiState.update { it.copy(isSearchingRemote = true) }
                val results = repository.searchAnime(query)
                _uiState.update {
                    it.copy(isSearchingRemote = false, remoteSearchResults = results)
                }
            }
        } else {
            _uiState.update { it.copy(isSearchingRemote = false, remoteSearchResults = null) }
        }
    }

    fun setFilterGenre(genre: String) {
        _uiState.update { it.copy(selectedGenre = genre) }
    }

    fun setFilterStatus(status: String) {
        _uiState.update { it.copy(selectedStatus = status) }
    }

    fun setFilterType(type: String) {
        _uiState.update { it.copy(selectedType = type) }
    }

    fun setFilterSeason(season: String) {
        _uiState.update { it.copy(selectedSeason = season) }
    }

    fun setFilterYear(year: Int?) {
        _uiState.update { it.copy(selectedYear = year) }
    }

    fun setSortOption(sortOption: SortOption) {
        _uiState.update { it.copy(selectedSort = sortOption) }
    }

    fun resetFilters() {
        _uiState.update {
            it.copy(
                searchQuery = "",
                selectedGenre = "الكل",
                selectedStatus = "الكل",
                selectedType = "الكل",
                selectedSeason = "الكل",
                selectedYear = null,
                selectedSort = SortOption.RATING,
                remoteSearchResults = null
            )
        }
    }

    // Anime Details Actions
    fun openAnimeDetails(anime: AnimeItem) {
        _uiState.update {
            it.copy(
                selectedAnime = anime,
                isLoadingEpisodes = true,
                selectedAnimeEpisodes = emptyList()
            )
        }
        viewModelScope.launch {
            val eps = repository.fetchEpisodes(anime)
            _uiState.update {
                it.copy(
                    selectedAnimeEpisodes = eps,
                    isLoadingEpisodes = false
                )
            }
        }
    }

    fun closeAnimeDetails() {
        _uiState.update { it.copy(selectedAnime = null, showTrailerDialog = false) }
    }

    fun toggleTrailerDialog(show: Boolean) {
        _uiState.update { it.copy(showTrailerDialog = show) }
    }

    // Watchlist & Episode State Actions
    fun setAnimeWatchlistStatus(anime: AnimeItem, status: WatchStatus?) {
        viewModelScope.launch {
            repository.setWatchlistStatus(anime, status)
            val msg = if (_uiState.value.isArabic) {
                if (status != null) "تمت الإضافة إلى قائمة: ${status.labelAr}" else "تمت الإزالة من مكتبتي"
            } else {
                if (status != null) "Added to ${status.labelEn}" else "Removed from Library"
            }
            showTemporaryBanner(msg)
        }
    }

    fun toggleEpisodeWatched(anime: AnimeItem, episodeNumber: Int) {
        viewModelScope.launch {
            val existing = episodeStates.value.find {
                it.malId == anime.malId && it.episodeNumber == episodeNumber
            }
            val currentWatched = existing?.isWatched ?: false
            repository.toggleEpisodeWatched(anime, episodeNumber, currentWatched, existing)
        }
    }

    fun downloadEpisode(anime: AnimeItem, episodeNumber: Int, quality: String, sizeMb: Int) {
        viewModelScope.launch {
            val existing = episodeStates.value.find {
                it.malId == anime.malId && it.episodeNumber == episodeNumber
            }
            repository.markEpisodeDownloaded(anime, episodeNumber, quality, sizeMb, existing)
            val msg = if (_uiState.value.isArabic) {
                "تم بدء وتخزين الحلقة $episodeNumber بجودة $quality ($sizeMb MB)"
            } else {
                "Episode $episodeNumber cached in $quality ($sizeMb MB)"
            }
            showTemporaryBanner(msg)
        }
    }

    fun downloadBatchEpisodes(anime: AnimeItem) {
        viewModelScope.launch {
            val episodes = _uiState.value.selectedAnimeEpisodes.take(6)
            episodes.forEach { ep ->
                val existing = episodeStates.value.find {
                    it.malId == anime.malId && it.episodeNumber == ep.number
                }
                repository.markEpisodeDownloaded(anime, ep.number, "1080p FHD", 320, existing)
            }
            val msg = if (_uiState.value.isArabic) {
                "تم تحميل دفعة (${episodes.size} حلقات) إلى مكتبة التحميلات بنجاح!"
            } else {
                "Batch downloaded (${episodes.size} episodes) to Library!"
            }
            showTemporaryBanner(msg)
        }
    }

    // Built-in Video Player Actions
    fun startEpisodePlayback(anime: AnimeItem, episode: AnimeEpisode? = null, resumeSeconds: Int = 0) {
        viewModelScope.launch {
            val episodes = if (_uiState.value.selectedAnime?.malId == anime.malId &&
                _uiState.value.selectedAnimeEpisodes.isNotEmpty()
            ) {
                _uiState.value.selectedAnimeEpisodes
            } else {
                repository.fetchEpisodes(anime)
            }
            val targetEpisode = episode ?: episodes.firstOrNull() ?: AnimeEpisode(
                malId = anime.malId,
                number = 1,
                titleEn = "Episode 1: Awakening",
                titleAr = "الحلقة 1: بداية الرحلة",
                airedDate = "2026-01-10"
            )
            val servers = repository.getServersForEpisode(anime, targetEpisode)
            val primaryServer = servers.first()
            val startPos = if (resumeSeconds > 0) resumeSeconds else 95

            val session = ActivePlayerSession(
                anime = anime,
                episode = targetEpisode,
                allEpisodes = episodes,
                servers = servers,
                selectedServer = primaryServer,
                selectedQuality = primaryServer.qualities.first(),
                playbackSpeed = 1.0f,
                currentSeconds = startPos,
                totalSeconds = 1440,
                isPlaying = true,
                isTheaterMode = false,
                isPipMinimized = false
            )
            _uiState.update { it.copy(activePlayerSession = session) }

            repository.recordWatchHistory(
                anime = anime,
                episode = targetEpisode,
                serverName = primaryServer.name,
                quality = primaryServer.qualities.first(),
                positionSeconds = startPos,
                durationSeconds = 1440
            )
            val existing = episodeStates.value.find {
                it.malId == anime.malId && it.episodeNumber == targetEpisode.number
            }
            if (existing?.isWatched != true) {
                repository.toggleEpisodeWatched(anime, targetEpisode.number, false, existing)
            }
        }
    }

    fun selectPlayerServer(server: StreamServer) {
        _uiState.update { state ->
            val current = state.activePlayerSession ?: return@update state
            val updated = current.copy(
                selectedServer = server,
                selectedQuality = server.qualities.firstOrNull() ?: "1080p FHD"
            )
            state.copy(activePlayerSession = updated)
        }
    }

    fun selectPlayerQuality(quality: String) {
        _uiState.update { state ->
            val current = state.activePlayerSession ?: return@update state
            state.copy(activePlayerSession = current.copy(selectedQuality = quality))
        }
    }

    fun selectPlaybackSpeed(speed: Float) {
        _uiState.update { state ->
            val current = state.activePlayerSession ?: return@update state
            state.copy(activePlayerSession = current.copy(playbackSpeed = speed))
        }
    }

    fun togglePlayerPlayPause() {
        _uiState.update { state ->
            val current = state.activePlayerSession ?: return@update state
            state.copy(activePlayerSession = current.copy(isPlaying = !current.isPlaying))
        }
    }

    fun seekPlayerTo(seconds: Int) {
        val current = _uiState.value.activePlayerSession ?: return
        val clamped = seconds.coerceIn(0, current.totalSeconds)
        _uiState.update { state ->
            state.copy(activePlayerSession = current.copy(currentSeconds = clamped))
        }
        viewModelScope.launch {
            repository.recordWatchHistory(
                anime = current.anime,
                episode = current.episode,
                serverName = current.selectedServer.name,
                quality = current.selectedQuality,
                positionSeconds = clamped,
                durationSeconds = current.totalSeconds
            )
        }
    }

    fun skipIntro85Seconds() {
        val current = _uiState.value.activePlayerSession ?: return
        seekPlayerTo(current.currentSeconds + 85)
    }

    fun playAdjacentEpisode(delta: Int) {
        val current = _uiState.value.activePlayerSession ?: return
        val currentIdx = current.allEpisodes.indexOfFirst { it.number == current.episode.number }
        val nextIdx = currentIdx + delta
        if (nextIdx in current.allEpisodes.indices) {
            val nextEp = current.allEpisodes[nextIdx]
            startEpisodePlayback(current.anime, nextEp, resumeSeconds = 15)
        }
    }

    fun toggleTheaterMode() {
        _uiState.update { state ->
            val current = state.activePlayerSession ?: return@update state
            state.copy(activePlayerSession = current.copy(isTheaterMode = !current.isTheaterMode))
        }
    }

    fun togglePipMode() {
        _uiState.update { state ->
            val current = state.activePlayerSession ?: return@update state
            state.copy(activePlayerSession = current.copy(isPipMinimized = !current.isPipMinimized))
        }
    }

    fun closeVideoPlayer() {
        _uiState.update { it.copy(activePlayerSession = null) }
    }

    fun clearWatchHistory() {
        viewModelScope.launch {
            repository.clearHistory()
        }
    }

    // Flagship Feature: Bihi Universal Web-Reader Actions
    fun updateScraperUrlInput(url: String) {
        _uiState.update { it.copy(scraperUrlInput = url) }
    }

    fun updateCustomSelectors(
        card: String,
        title: String,
        poster: String,
        episode: String,
        iframe: String
    ) {
        _uiState.update {
            it.copy(
                customCardSelector = card,
                customTitleSelector = title,
                customPosterSelector = poster,
                customEpisodeSelector = episode,
                customIframeSelector = iframe
            )
        }
    }

    fun runUniversalWebScraper(urlOverride: String? = null, saveToSources: Boolean = true) {
        val targetUrl = (urlOverride ?: _uiState.value.scraperUrlInput).trim()
        if (targetUrl.isBlank()) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    scraperUrlInput = targetUrl,
                    isScraping = true,
                    scraperProgress = 0.05f,
                    scraperLogs = emptyList()
                )
            }
            val stateSnap = _uiState.value
            val catalogResult = repository.runScraper(
                rawUrl = targetUrl,
                cardSelector = stateSnap.customCardSelector,
                titleSelector = stateSnap.customTitleSelector,
                posterSelector = stateSnap.customPosterSelector,
                episodeSelector = stateSnap.customEpisodeSelector,
                iframeSelector = stateSnap.customIframeSelector,
                baseCatalog = stateSnap.catalog,
                saveSourceToDb = saveToSources,
                onStepUpdate = { logs, progress ->
                    _uiState.update { s ->
                        s.copy(scraperLogs = logs, scraperProgress = progress)
                    }
                }
            )
            _uiState.update {
                it.copy(
                    isScraping = false,
                    scraperProgress = 1.0f,
                    activeScrapedCatalog = catalogResult,
                    scrapedCatalogSearchQuery = ""
                )
            }
        }
    }

    fun openSavedScraperSource(source: ScraperSourceEntity) {
        val catalog = repository.getCatalogForSavedSource(source, _uiState.value.catalog)
        _uiState.update {
            it.copy(
                scraperUrlInput = source.url,
                customCardSelector = source.cardSelector,
                customTitleSelector = source.titleSelector,
                customPosterSelector = source.posterSelector,
                customEpisodeSelector = source.episodeSelector,
                customIframeSelector = source.iframeSelector,
                activeScrapedCatalog = catalog,
                scrapedCatalogSearchQuery = ""
            )
        }
    }

    fun closeScrapedCatalogView() {
        _uiState.update { it.copy(activeScrapedCatalog = null) }
    }

    fun updateScrapedCatalogSearch(query: String) {
        _uiState.update { it.copy(scrapedCatalogSearchQuery = query) }
    }

    fun deleteScraperSource(id: Int) {
        viewModelScope.launch {
            repository.deleteScraperSource(id)
        }
    }

    // Community Reviews
    fun submitCommunityReview(malId: Int, author: String, rating: Int, comment: String, isSpoiler: Boolean) {
        if (comment.isBlank()) return
        viewModelScope.launch {
            repository.addCommunityReview(malId, author, rating, comment, isSpoiler)
        }
    }

    fun likeCommunityReview(reviewId: Int) {
        viewModelScope.launch {
            repository.likeReview(reviewId)
        }
    }

    private fun showTemporaryBanner(message: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(statusBannerMessage = message) }
            delay(2600)
            _uiState.update {
                if (it.statusBannerMessage == message) it.copy(statusBannerMessage = null) else it
            }
        }
    }

    class Factory(private val repository: BihiRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BihiViewModel(repository) as T
        }
    }
}
