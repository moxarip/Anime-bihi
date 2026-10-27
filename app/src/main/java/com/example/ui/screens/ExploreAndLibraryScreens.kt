package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.EpisodeStateEntity
import com.example.data.local.ScraperSourceEntity
import com.example.data.local.WatchHistoryEntity
import com.example.data.local.WatchlistEntity
import com.example.data.model.AnimeItem
import com.example.data.model.WatchStatus
import com.example.ui.components.AnimePosterCard
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.CanonEmerald
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.ElectricCrimson
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.RichObsidian
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.SortOption

@Composable
fun ExploreAndFilterScreen(
    isArabic: Boolean,
    catalog: List<AnimeItem>,
    remoteSearchResults: List<AnimeItem>?,
    isSearchingRemote: Boolean,
    searchQuery: String,
    selectedGenre: String,
    selectedStatus: String,
    selectedType: String,
    selectedSeason: String,
    selectedYear: Int?,
    selectedSort: SortOption,
    onUpdateSearchQuery: (String) -> Unit,
    onSelectGenre: (String) -> Unit,
    onSelectStatus: (String) -> Unit,
    onSelectType: (String) -> Unit,
    onSelectSeason: (String) -> Unit,
    onSelectYear: (Int?) -> Unit,
    onSelectSort: (SortOption) -> Unit,
    onResetFilters: () -> Unit,
    onOpenAnimeDetails: (AnimeItem) -> Unit
) {
    var showFilterDrawer by remember { mutableStateOf(true) }

    val genresList = listOf(
        "الكل" to "All",
        "Action" to "أكشن",
        "Fantasy" to "خيال",
        "Adventure" to "مغامرات",
        "Shounen" to "شونين",
        "Sci-Fi" to "خيال علمي",
        "Drama" to "دراما",
        "Supernatural" to "خارق للطبيعة",
        "Suspense" to "إثارة وتشويق",
        "Psychological" to "نفسي",
        "Sports" to "رياضة",
        "Comedy" to "كوميديا"
    )
    val statusesList = listOf(
        "الكل" to (if (isArabic) "كل الحالات" else "All Status"),
        "Airing" to (if (isArabic) "مستمر (Airing)" else "Airing"),
        "Completed" to (if (isArabic) "مكتمل (Completed)" else "Completed"),
        "Upcoming" to (if (isArabic) "قريباً (Upcoming)" else "Upcoming")
    )
    val typesList = listOf("الكل", "TV", "Movie", "OVA", "ONA")
    val seasonsList = listOf(
        "الكل" to (if (isArabic) "كل المواسم" else "All Seasons"),
        "Winter" to (if (isArabic) "شتاء (Winter)" else "Winter"),
        "Spring" to (if (isArabic) "ربيع (Spring)" else "Spring"),
        "Summer" to (if (isArabic) "صيف (Summer)" else "Summer"),
        "Fall" to (if (isArabic) "خريف (Fall)" else "Fall")
    )
    val yearsList = listOf(null, 2025, 2024, 2023, 2011, 2009)

    val sourcePool = remoteSearchResults ?: catalog
    val filteredPool = sourcePool.filter { item ->
        val matchesQuery = searchQuery.isBlank() ||
            item.titleAr.contains(searchQuery, ignoreCase = true) ||
            item.titleEn.contains(searchQuery, ignoreCase = true) ||
            item.titleRomaji.contains(searchQuery, ignoreCase = true) ||
            item.studio.contains(searchQuery, ignoreCase = true)

        val matchesGenre = selectedGenre == "الكل" ||
            item.genresEn.any { it.equals(selectedGenre, ignoreCase = true) }

        val matchesStatus = selectedStatus == "الكل" ||
            item.statusEn.equals(selectedStatus, ignoreCase = true)

        val matchesType = selectedType == "الكل" ||
            item.type.equals(selectedType, ignoreCase = true)

        val matchesSeason = selectedSeason == "الكل" ||
            item.season.equals(selectedSeason, ignoreCase = true)

        val matchesYear = selectedYear == null || item.year == selectedYear

        matchesQuery && matchesGenre && matchesStatus && matchesType && matchesSeason && matchesYear
    }.let { list ->
        when (selectedSort) {
            SortOption.RATING -> list.sortedByDescending { it.score }
            SortOption.POPULARITY -> list.sortedBy { it.popularity }
            SortOption.LATEST -> list.sortedByDescending { it.year }
            SortOption.ALPHABETICAL -> list.sortedBy { if (isArabic) it.titleAr else it.titleEn }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .testTag("explore_screen"),
        contentPadding = PaddingValues(16.dp, 14.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Instant Search Bar + Filter Drawer Toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = onUpdateSearchQuery,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("explore_search_input"),
                    singleLine = true,
                    placeholder = {
                        Text(
                            text = if (isArabic) {
                                "ابحث عن أنمي، استوديو، أو شخصية (مباشر من Jikan API)..."
                            } else {
                                "Instant search anime, studio, romaji (Live Jikan v4)..."
                            }
                        )
                    },
                    leadingIcon = {
                        if (isSearchingRemote) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = ElectricCrimson,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null,
                                tint = ElectricCrimson
                            )
                        }
                    },
                    trailingIcon = {
                        if (searchQuery.isNotBlank()) {
                            IconButton(onClick = { onUpdateSearchQuery("") }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = TextSecondary
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = ElectricCrimson,
                        unfocusedBorderColor = GlassBorder,
                        focusedContainerColor = RichObsidian,
                        unfocusedContainerColor = RichObsidian
                    ),
                    shape = RoundedCornerShape(14.dp)
                )

                Surface(
                    onClick = { showFilterDrawer = !showFilterDrawer },
                    color = if (showFilterDrawer) ElectricCrimson else ObsidianElevated,
                    shape = RoundedCornerShape(14.dp),
                    border = BorderStroke(1.dp, GlassBorder),
                    modifier = Modifier
                        .height(54.dp)
                        .testTag("toggle_filter_drawer_btn")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "Filters",
                            tint = Color.White
                        )
                        Text(
                            text = if (isArabic) "الفلترة" else "Filters",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }
                }
            }
        }

        // 2. Multi-Layered Anime Slayer Filter Drawer
        item {
            AnimatedVisibility(visible = showFilterDrawer) {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    backgroundColor = RichObsidian
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Genres Row
                        FilterLabelRow(
                            title = if (isArabic) "التصنيفات (Genres)" else "Genres",
                            actionText = if (isArabic) "إعادة ضبط" else "Reset All",
                            onAction = onResetFilters
                        )
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(genresList, key = { it.first }) { (key, labelAr) ->
                                val active = selectedGenre == key
                                FilterPill(
                                    text = if (isArabic) (if (key == "الكل") "الكل" else labelAr) else (if (key == "الكل") "All" else key),
                                    selected = active,
                                    onClick = { onSelectGenre(key) }
                                )
                            }
                        }

                        // Status & Type Row
                        FilterLabelRow(title = if (isArabic) "الحالة والنوع (Status & Type)" else "Status & Format")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(statusesList, key = { "st_${it.first}" }) { (key, label) ->
                                FilterPill(
                                    text = label,
                                    selected = selectedStatus == key,
                                    onClick = { onSelectStatus(key) }
                                )
                            }
                            items(typesList, key = { "tp_$it" }) { typeKey ->
                                FilterPill(
                                    text = if (typeKey == "الكل") (if (isArabic) "كل الأنواع" else "All Types") else typeKey,
                                    selected = selectedType == typeKey,
                                    activeColor = CyberPurple,
                                    onClick = { onSelectType(typeKey) }
                                )
                            }
                        }

                        // Season & Year Row
                        FilterLabelRow(title = if (isArabic) "الموسم والسنة (Season & Year)" else "Season & Year")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(seasonsList, key = { "sn_${it.first}" }) { (key, label) ->
                                FilterPill(
                                    text = label,
                                    selected = selectedSeason == key,
                                    activeColor = CyberCyan,
                                    onClick = { onSelectSeason(key) }
                                )
                            }
                            items(yearsList, key = { "yr_${it ?: 0}" }) { yearVal ->
                                FilterPill(
                                    text = yearVal?.toString() ?: (if (isArabic) "كل السنوات" else "All Years"),
                                    selected = selectedYear == yearVal,
                                    activeColor = CanonEmerald,
                                    onClick = { onSelectYear(yearVal) }
                                )
                            }
                        }

                        // Sorting Row
                        FilterLabelRow(title = if (isArabic) "الترتيب حسب (Sort By)" else "Sort By")
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            items(SortOption.entries, key = { it.name }) { sortOpt ->
                                FilterPill(
                                    text = if (isArabic) sortOpt.labelAr else sortOpt.labelEn,
                                    selected = selectedSort == sortOpt,
                                    onClick = { onSelectSort(sortOpt) }
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Results Count Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (isArabic) {
                        "نتائج البحث والفلترة (${filteredPool.size} أنمي)"
                    } else {
                        "Filtered Results (${filteredPool.size} series)"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 4. Filtered Anime Grid (2 columns per row)
        val rows = filteredPool.chunked(2)
        items(rows.size, key = { "exp_row_$it" }) { idx ->
            val pair = rows[idx]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                pair.forEach { anime ->
                    Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        AnimePosterCard(
                            anime = anime,
                            isArabic = isArabic,
                            showEpisodeBadge = true,
                            width = 185.dp,
                            onClick = { onOpenAnimeDetails(anime) }
                        )
                    }
                }
                if (pair.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun FilterLabelRow(
    title: String,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondary,
            fontWeight = FontWeight.Bold
        )
        if (actionText != null && onAction != null) {
            Row(
                modifier = Modifier.clickable { onAction() },
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.RestartAlt,
                    contentDescription = null,
                    tint = ElectricCrimson,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCrimson
                )
            }
        }
    }
}

@Composable
private fun FilterPill(
    text: String,
    selected: Boolean,
    activeColor: Color = ElectricCrimson,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(50),
        color = if (selected) activeColor else ObsidianElevated,
        border = BorderStroke(1.dp, if (selected) activeColor else GlassBorder)
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun UserLibraryScreen(
    isArabic: Boolean,
    catalog: List<AnimeItem>,
    watchlist: List<WatchlistEntity>,
    watchHistory: List<WatchHistoryEntity>,
    episodeStates: List<EpisodeStateEntity>,
    savedSources: List<ScraperSourceEntity>,
    onOpenAnimeById: (Int) -> Unit,
    onResumeHistory: (WatchHistoryEntity) -> Unit,
    onClearHistory: () -> Unit,
    onOpenSavedSource: (ScraperSourceEntity) -> Unit
) {
    var selectedSection by remember { mutableIntStateOf(0) }
    var selectedWatchStatus by remember { mutableStateOf<WatchStatus?>(null) }

    val downloadedEpisodes = episodeStates.filter { it.isDownloaded }

    val sections = listOf(
        0 to (if (isArabic) "قائمتي (${watchlist.size})" else "Watchlist (${watchlist.size})"),
        1 to (if (isArabic) "سجل المشاهدة (${watchHistory.size})" else "History (${watchHistory.size})"),
        2 to (if (isArabic) "التحميلات (${downloadedEpisodes.size})" else "Downloads (${downloadedEpisodes.size})"),
        3 to (if (isArabic) "المصادر (${savedSources.size})" else "Sources (${savedSources.size})")
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .testTag("library_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Library Top Sub-Tabs
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(sections, key = { it.first }) { (idx, title) ->
                    val active = selectedSection == idx
                    Surface(
                        onClick = { selectedSection = idx },
                        shape = RoundedCornerShape(12.dp),
                        color = if (active) ElectricCrimson else ObsidianElevated,
                        border = BorderStroke(1.dp, if (active) ElectricCrimson else GlassBorder),
                        modifier = Modifier.testTag("library_tab_$idx")
                    ) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }

        // Section 0: Categorized Watchlist (أشاهده، مكتمل، أخطط لمشاهدته، متوقف، المفضلة)
        if (selectedSection == 0) {
            item {
                LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item {
                        FilterPill(
                            text = if (isArabic) "الكل (${watchlist.size})" else "All (${watchlist.size})",
                            selected = selectedWatchStatus == null,
                            activeColor = CyberPurple,
                            onClick = { selectedWatchStatus = null }
                        )
                    }
                    items(WatchStatus.entries, key = { it.name }) { status ->
                        val count = watchlist.count { it.statusCategory == status.name }
                        FilterPill(
                            text = "${if (isArabic) status.labelAr else status.labelEn} ($count)",
                            selected = selectedWatchStatus == status,
                            activeColor = CyberPurple,
                            onClick = { selectedWatchStatus = status }
                        )
                    }
                }
            }

            val filteredWatchlist = watchlist.filter {
                selectedWatchStatus == null || it.statusCategory == selectedWatchStatus?.name
            }

            if (filteredWatchlist.isEmpty()) {
                item {
                    EmptyLibraryCard(
                        icon = Icons.Default.Bookmark,
                        title = if (isArabic) "قائمة المشاهدة فارغة حالياً" else "Your Watchlist is Empty",
                        subtitle = if (isArabic) {
                            "أضف أنمياتك المفضلة من الصفحة الرئيسية أو صفحة التفاصيل لتظهر هنا مصنفة."
                        } else {
                            "Add anime from Home or Details to organize your categorized watchlist."
                        }
                    )
                }
            } else {
                items(filteredWatchlist, key = { "wl_${it.malId}" }) { entry ->
                    val statusObj = WatchStatus.entries.find { it.name == entry.statusCategory } ?: WatchStatus.WATCHING
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onOpenAnimeById(entry.malId) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = entry.posterUrl,
                                contentDescription = entry.titleAr,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .width(64.dp)
                                    .height(90.dp)
                                    .clip(RoundedCornerShape(10.dp))
                                    .background(ObsidianElevated)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) entry.titleAr else entry.titleEn,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "${entry.studio} • ${entry.episodesCount} EP",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CyberPurple.copy(alpha = 0.25f))
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (isArabic) statusObj.labelAr else statusObj.labelEn,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFDDD6FE)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Section 1: Watch History with Timestamp Resume
        if (selectedSection == 1) {
            if (watchHistory.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isArabic) "استأنف المشاهدة من آخر توقيت" else "Resume from saved timestamp",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                        Surface(
                            onClick = onClearHistory,
                            color = ElectricCrimson.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    tint = ElectricCrimson,
                                    modifier = Modifier.size(16.dp)
                                )
                                Text(
                                    text = if (isArabic) "مسح السجل" else "Clear History",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricCrimson
                                )
                            }
                        }
                    }
                }
            }

            if (watchHistory.isEmpty()) {
                item {
                    EmptyLibraryCard(
                        icon = Icons.Default.History,
                        title = if (isArabic) "لا يوجد سجل مشاهدة بعد" else "No Watch History Yet",
                        subtitle = if (isArabic) {
                            "يتم حفظ الدقيقة والثانية تلقائياً عند تشغيل أي حلقة في مشغل أنمي بيهي."
                        } else {
                            "Timestamps are automatically persisted when streaming any episode."
                        }
                    )
                }
            } else {
                items(watchHistory, key = { "hist_${it.malId}_${it.episodeNumber}" }) { hist ->
                    val progress = (hist.positionSeconds.toFloat() / hist.durationSeconds.coerceAtLeast(1)).coerceIn(0.05f, 1f)
                    val posMin = hist.positionSeconds / 60
                    val posSec = (hist.positionSeconds % 60).toString().padStart(2, '0')
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onResumeHistory(hist) }
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                AsyncImage(
                                    model = hist.posterUrl,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isArabic) hist.animeTitleAr else hist.animeTitleEn,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (isArabic) hist.episodeTitleAr else hist.episodeTitleEn,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = CyberCyan,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "${hist.serverName} • ${hist.quality} • $posMin:$posSec / 24:00",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }
                                Surface(
                                    onClick = { onResumeHistory(hist) },
                                    color = ElectricCrimson,
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Text(
                                            text = if (isArabic) "استئناف" else "Resume",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(4.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = ElectricCrimson,
                                trackColor = Color.White.copy(alpha = 0.1f)
                            )
                        }
                    }
                }
            }
        }

        // Section 2: Downloaded / Cached Episodes Tracker
        if (selectedSection == 2) {
            if (downloadedEpisodes.isEmpty()) {
                item {
                    EmptyLibraryCard(
                        icon = Icons.Default.CloudDone,
                        title = if (isArabic) "لا توجد حلقات محملة حالياً" else "No Cached Episodes Yet",
                        subtitle = if (isArabic) {
                            "يمكنك تحميل أي حلقة أو دفعة حلقات من صفحة الأنمي أو مشغل الفيديو للمشاهدة دون اتصال."
                        } else {
                            "Download individual episodes or batches from the Anime Details or Video Player."
                        }
                    )
                }
            } else {
                items(downloadedEpisodes, key = { "dl_${it.malId}_${it.episodeNumber}" }) { epState ->
                    GlassmorphicCard(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = { onOpenAnimeById(epState.malId) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = epState.posterUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .clip(RoundedCornerShape(10.dp))
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isArabic) epState.animeTitleAr else epState.animeTitleEn,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = if (isArabic) {
                                        "الحلقة ${epState.episodeNumber} • تم التخزين (${epState.downloadQuality})"
                                    } else {
                                        "Episode ${epState.episodeNumber} • Cached (${epState.downloadQuality})"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = CanonEmerald
                                )
                            }
                            Text(
                                text = "${epState.downloadSizeMb} MB",
                                style = MaterialTheme.typography.labelMedium,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Section 3: Saved Scraper Sources
        if (selectedSection == 3) {
            items(savedSources, key = { "lib_src_${it.id}" }) { src ->
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onOpenSavedSource(src) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Public,
                            contentDescription = null,
                            tint = CyberPurple
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = src.name,
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "${src.url} • ${src.statusText}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CanonEmerald
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyLibraryCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CyberPurple,
                modifier = Modifier.size(42.dp)
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
