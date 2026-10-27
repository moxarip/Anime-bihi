package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Public
import androidx.compose.material.icons.outlined.VideoLibrary
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.local.BihiDatabase
import com.example.data.model.WatchStatus
import com.example.data.repository.BihiRepository
import com.example.ui.components.BihiTopHeader
import com.example.ui.screens.AnimeDetailScreen
import com.example.ui.screens.ExploreAndFilterScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ScheduleScreen
import com.example.ui.screens.UserLibraryScreen
import com.example.ui.screens.VideoPlayerModal
import com.example.ui.screens.WebReaderScreen
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.AnimeBihiTheme
import com.example.ui.theme.CanonEmerald
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.ElectricCrimson
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.RichObsidian
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.BihiViewModel
import com.example.ui.viewmodel.MainNavTab

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AnimeBihiAppRoot()
        }
    }
}

@Composable
fun AnimeBihiAppRoot() {
    val context = LocalContext.current
    val repository = remember {
        val db = BihiDatabase.getInstance(context)
        BihiRepository(db.bihiDao())
    }
    val viewModel: BihiViewModel = viewModel(factory = BihiViewModel.Factory(repository))

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val watchlist by viewModel.watchlist.collectAsStateWithLifecycle()
    val watchHistory by viewModel.watchHistory.collectAsStateWithLifecycle()
    val episodeStates by viewModel.episodeStates.collectAsStateWithLifecycle()
    val scraperSources by viewModel.scraperSources.collectAsStateWithLifecycle()
    val communityReviews by viewModel.communityReviews.collectAsStateWithLifecycle()

    // Handle Back button on non-Home tabs when no modal is open
    if (uiState.selectedAnime == null &&
        (uiState.activePlayerSession == null || uiState.activePlayerSession?.isPipMinimized == true) &&
        uiState.currentTab != MainNavTab.HOME
    ) {
        BackHandler {
            viewModel.selectTab(MainNavTab.HOME)
        }
    }

    AnimeBihiTheme(isRtl = uiState.isArabic) {
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .background(AmoledBlack)
        ) {
            val isExpandedScreen = maxWidth >= 700.dp

            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = AmoledBlack,
                contentWindowInsets = WindowInsets.safeDrawing,
                topBar = {
                    BihiTopHeader(
                        isArabic = uiState.isArabic,
                        onToggleLanguage = { viewModel.toggleLanguage() },
                        onOpenSearch = { viewModel.selectTab(MainNavTab.EXPLORE) },
                        onOpenWebReader = { viewModel.selectTab(MainNavTab.WEB_READER) }
                    )
                },
                bottomBar = {
                    if (!isExpandedScreen) {
                        BihiBottomNavigationBar(
                            currentTab = uiState.currentTab,
                            isArabic = uiState.isArabic,
                            onSelectTab = { tab ->
                                viewModel.closeAnimeDetails()
                                viewModel.selectTab(tab)
                            }
                        )
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (isExpandedScreen) {
                        BihiSideNavigationRail(
                            currentTab = uiState.currentTab,
                            isArabic = uiState.isArabic,
                            onSelectTab = { tab ->
                                viewModel.closeAnimeDetails()
                                viewModel.selectTab(tab)
                            }
                        )
                    }

                    Box(modifier = Modifier.weight(1f).fillMaxHeight()) {
                        // Primary Tab Content
                        when (uiState.currentTab) {
                            MainNavTab.HOME -> HomeScreen(
                                isArabic = uiState.isArabic,
                                catalog = uiState.catalog,
                                seasonalCatalog = uiState.seasonalCatalog,
                                watchHistory = watchHistory,
                                watchlist = watchlist,
                                heroIndex = uiState.heroIndex,
                                onSetHeroIndex = { viewModel.setHeroIndex(it) },
                                onOpenAnimeDetails = { viewModel.openAnimeDetails(it) },
                                onWatchNow = { viewModel.startEpisodePlayback(it) },
                                onToggleWatchlist = { anime ->
                                    val exists = watchlist.any { it.malId == anime.malId }
                                    viewModel.setAnimeWatchlistStatus(
                                        anime,
                                        if (exists) null else WatchStatus.FAVORITE
                                    )
                                },
                                onOpenScheduleTab = { viewModel.selectTab(MainNavTab.SCHEDULE) },
                                onOpenWebReaderTab = { viewModel.selectTab(MainNavTab.WEB_READER) },
                                onOpenExploreTab = { viewModel.selectTab(MainNavTab.EXPLORE) }
                            )

                            MainNavTab.SCHEDULE -> ScheduleScreen(
                                isArabic = uiState.isArabic,
                                catalog = uiState.catalog,
                                selectedDay = uiState.selectedDay,
                                onSelectDay = { viewModel.selectScheduleDay(it) },
                                onOpenAnimeDetails = { viewModel.openAnimeDetails(it) },
                                onWatchNow = { viewModel.startEpisodePlayback(it) }
                            )

                            MainNavTab.WEB_READER -> WebReaderScreen(
                                isArabic = uiState.isArabic,
                                urlInput = uiState.scraperUrlInput,
                                cardSelector = uiState.customCardSelector,
                                titleSelector = uiState.customTitleSelector,
                                posterSelector = uiState.customPosterSelector,
                                episodeSelector = uiState.customEpisodeSelector,
                                iframeSelector = uiState.customIframeSelector,
                                isScraping = uiState.isScraping,
                                scraperProgress = uiState.scraperProgress,
                                scraperLogs = uiState.scraperLogs,
                                savedSources = scraperSources,
                                activeScrapedCatalog = uiState.activeScrapedCatalog,
                                scrapedSearchQuery = uiState.scrapedCatalogSearchQuery,
                                onUpdateUrl = { viewModel.updateScraperUrlInput(it) },
                                onUpdateSelectors = { c, t, p, e, i ->
                                    viewModel.updateCustomSelectors(c, t, p, e, i)
                                },
                                onRunScraper = { urlOverride ->
                                    viewModel.runUniversalWebScraper(urlOverride = urlOverride, saveToSources = true)
                                },
                                onOpenSavedSource = { viewModel.openSavedScraperSource(it) },
                                onDeleteSavedSource = { viewModel.deleteScraperSource(it) },
                                onCloseScrapedCatalog = { viewModel.closeScrapedCatalogView() },
                                onUpdateScrapedSearch = { viewModel.updateScrapedCatalogSearch(it) },
                                onOpenAnimeDetails = { viewModel.openAnimeDetails(it) },
                                onStreamAnimeDirect = { viewModel.startEpisodePlayback(it) }
                            )

                            MainNavTab.LIBRARY -> UserLibraryScreen(
                                isArabic = uiState.isArabic,
                                catalog = uiState.catalog,
                                watchlist = watchlist,
                                watchHistory = watchHistory,
                                episodeStates = episodeStates,
                                savedSources = scraperSources,
                                onOpenAnimeById = { malId ->
                                    val found = uiState.catalog.find { it.malId == malId }
                                        ?: uiState.catalog.firstOrNull()
                                    if (found != null) viewModel.openAnimeDetails(found)
                                },
                                onResumeHistory = { hist ->
                                    val found = uiState.catalog.find { it.malId == hist.malId }
                                        ?: uiState.catalog.firstOrNull()
                                    if (found != null) {
                                        viewModel.startEpisodePlayback(found, resumeSeconds = hist.positionSeconds)
                                    }
                                },
                                onClearHistory = { viewModel.clearWatchHistory() },
                                onOpenSavedSource = { src ->
                                    viewModel.openSavedScraperSource(src)
                                    viewModel.selectTab(MainNavTab.WEB_READER)
                                }
                            )

                            MainNavTab.EXPLORE -> ExploreAndFilterScreen(
                                isArabic = uiState.isArabic,
                                catalog = uiState.catalog,
                                remoteSearchResults = uiState.remoteSearchResults,
                                isSearchingRemote = uiState.isSearchingRemote,
                                searchQuery = uiState.searchQuery,
                                selectedGenre = uiState.selectedGenre,
                                selectedStatus = uiState.selectedStatus,
                                selectedType = uiState.selectedType,
                                selectedSeason = uiState.selectedSeason,
                                selectedYear = uiState.selectedYear,
                                selectedSort = uiState.selectedSort,
                                onUpdateSearchQuery = { viewModel.updateSearchQuery(it) },
                                onSelectGenre = { viewModel.setFilterGenre(it) },
                                onSelectStatus = { viewModel.setFilterStatus(it) },
                                onSelectType = { viewModel.setFilterType(it) },
                                onSelectSeason = { viewModel.setFilterSeason(it) },
                                onSelectYear = { viewModel.setFilterYear(it) },
                                onSelectSort = { viewModel.setSortOption(it) },
                                onResetFilters = { viewModel.resetFilters() },
                                onOpenAnimeDetails = { viewModel.openAnimeDetails(it) }
                            )
                        }

                        // Secondary Overlay 1: Rich Anime Details Screen
                        val selectedAnime = uiState.selectedAnime
                        if (selectedAnime != null) {
                            AnimeDetailScreen(
                                anime = selectedAnime,
                                episodes = uiState.selectedAnimeEpisodes,
                                isLoadingEpisodes = uiState.isLoadingEpisodes,
                                isArabic = uiState.isArabic,
                                watchlistEntries = watchlist,
                                episodeStates = episodeStates,
                                reviews = communityReviews,
                                showTrailerDialog = uiState.showTrailerDialog,
                                onCloseDetails = { viewModel.closeAnimeDetails() },
                                onPlayEpisode = { ep ->
                                    viewModel.startEpisodePlayback(selectedAnime, ep)
                                },
                                onSetWatchStatus = { status ->
                                    viewModel.setAnimeWatchlistStatus(selectedAnime, status)
                                },
                                onToggleEpisodeWatched = { epNum ->
                                    viewModel.toggleEpisodeWatched(selectedAnime, epNum)
                                },
                                onDownloadEpisode = { epNum, quality, sizeMb ->
                                    viewModel.downloadEpisode(selectedAnime, epNum, quality, sizeMb)
                                },
                                onDownloadBatch = {
                                    viewModel.downloadBatchEpisodes(selectedAnime)
                                },
                                onToggleTrailer = { show ->
                                    viewModel.toggleTrailerDialog(show)
                                },
                                onAddReview = { author, rating, comment, isSpoiler ->
                                    viewModel.submitCommunityReview(
                                        selectedAnime.malId,
                                        author,
                                        rating,
                                        comment,
                                        isSpoiler
                                    )
                                },
                                onLikeReview = { reviewId ->
                                    viewModel.likeCommunityReview(reviewId)
                                }
                            )
                        }

                        // Secondary Overlay 2: Built-in Multi-Server Video Player Modal (Full or PiP)
                        val activeSession = uiState.activePlayerSession
                        if (activeSession != null) {
                            VideoPlayerModal(
                                session = activeSession,
                                isArabic = uiState.isArabic,
                                onSelectServer = { viewModel.selectPlayerServer(it) },
                                onSelectQuality = { viewModel.selectPlayerQuality(it) },
                                onSelectSpeed = { viewModel.selectPlaybackSpeed(it) },
                                onTogglePlayPause = { viewModel.togglePlayerPlayPause() },
                                onSeekTo = { viewModel.seekPlayerTo(it) },
                                onSkipIntro85s = { viewModel.skipIntro85Seconds() },
                                onPlayAdjacentEpisode = { viewModel.playAdjacentEpisode(it) },
                                onToggleTheaterMode = { viewModel.toggleTheaterMode() },
                                onTogglePipMode = { viewModel.togglePipMode() },
                                onDownloadEpisode = { epNum, quality, sizeMb ->
                                    viewModel.downloadEpisode(activeSession.anime, epNum, quality, sizeMb)
                                },
                                onSelectEpisodeDirect = { ep ->
                                    viewModel.startEpisodePlayback(activeSession.anime, ep, resumeSeconds = 15)
                                },
                                onClosePlayer = { viewModel.closeVideoPlayer() }
                            )
                        }

                        // Floating Feedback Banner Toast
                        if (uiState.statusBannerMessage != null) {
                            Surface(
                                color = ObsidianElevated,
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .align(Alignment.TopCenter)
                                    .padding(top = 12.dp, start = 20.dp, end = 20.dp)
                                    .border(1.dp, CanonEmerald, RoundedCornerShape(12.dp))
                            ) {
                                Text(
                                    text = uiState.statusBannerMessage.orEmpty(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CanonEmerald,
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private data class NavTabMeta(
    val tab: MainNavTab,
    val activeIcon: ImageVector,
    val inactiveIcon: ImageVector,
    val testTag: String
)

private val navTabsMetadata = listOf(
    NavTabMeta(MainNavTab.HOME, Icons.Filled.Home, Icons.Outlined.Home, "nav_tab_home"),
    NavTabMeta(MainNavTab.SCHEDULE, Icons.Filled.CalendarMonth, Icons.Outlined.CalendarMonth, "nav_tab_schedule"),
    NavTabMeta(MainNavTab.WEB_READER, Icons.Filled.Public, Icons.Outlined.Public, "nav_tab_web_reader"),
    NavTabMeta(MainNavTab.LIBRARY, Icons.Filled.VideoLibrary, Icons.Outlined.VideoLibrary, "nav_tab_library"),
    NavTabMeta(MainNavTab.EXPLORE, Icons.Filled.Explore, Icons.Outlined.Explore, "nav_tab_explore")
)

@Composable
private fun BihiBottomNavigationBar(
    currentTab: MainNavTab,
    isArabic: Boolean,
    onSelectTab: (MainNavTab) -> Unit
) {
    NavigationBar(
        containerColor = RichObsidian.copy(alpha = 0.96f),
        tonalElevation = 8.dp,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, GlassBorder)
            .windowInsetsPadding(WindowInsets.navigationBars)
    ) {
        navTabsMetadata.forEach { item ->
            val selected = currentTab == item.tab
            val isFlagshipScraper = item.tab == MainNavTab.WEB_READER
            NavigationBarItem(
                selected = selected,
                onClick = { onSelectTab(item.tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.activeIcon else item.inactiveIcon,
                        contentDescription = if (isArabic) item.tab.labelAr else item.tab.labelEn
                    )
                },
                label = {
                    Text(
                        text = if (isArabic) item.tab.labelAr else item.tab.labelEn,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected || isFlagshipScraper) FontWeight.Bold else FontWeight.Normal
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = Color.White,
                    selectedTextColor = if (isFlagshipScraper) CyberPurple else ElectricCrimson,
                    indicatorColor = if (isFlagshipScraper) CyberPurple else ElectricCrimson,
                    unselectedIconColor = TextSecondary,
                    unselectedTextColor = TextSecondary
                ),
                modifier = Modifier.testTag(item.testTag)
            )
        }
    }
}

@Composable
private fun BihiSideNavigationRail(
    currentTab: MainNavTab,
    isArabic: Boolean,
    onSelectTab: (MainNavTab) -> Unit
) {
    NavigationRail(
        containerColor = RichObsidian,
        modifier = Modifier
            .fillMaxHeight()
            .border(1.dp, GlassBorder)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            navTabsMetadata.forEach { item ->
                val selected = currentTab == item.tab
                NavigationRailItem(
                    selected = selected,
                    onClick = { onSelectTab(item.tab) },
                    icon = {
                        Icon(
                            imageVector = if (selected) item.activeIcon else item.inactiveIcon,
                            contentDescription = if (isArabic) item.tab.labelAr else item.tab.labelEn
                        )
                    },
                    label = {
                        Text(
                            text = if (isArabic) item.tab.labelAr else item.tab.labelEn,
                            style = MaterialTheme.typography.labelSmall
                        )
                    },
                    colors = NavigationRailItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = ElectricCrimson,
                        indicatorColor = ElectricCrimson,
                        unselectedIconColor = TextSecondary,
                        unselectedTextColor = TextSecondary
                    ),
                    modifier = Modifier
                        .padding(vertical = 4.dp)
                        .testTag(item.testTag)
                )
            }
        }
    }
}
