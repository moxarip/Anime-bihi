package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BookmarkAdd
import androidx.compose.material.icons.filled.BookmarkAdded
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.WatchHistoryEntity
import com.example.data.local.WatchlistEntity
import com.example.data.model.AnimeDayOfWeek
import com.example.data.model.AnimeItem
import com.example.data.model.WatchStatus
import com.example.ui.components.AnimePosterCard
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.SectionHeaderRow
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.BihiPrimaryGradient
import com.example.ui.theme.CanonEmerald
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.ElectricCrimson
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.RichObsidian
import com.example.ui.theme.StarGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay
import java.util.Locale

@Composable
fun HomeScreen(
    isArabic: Boolean,
    catalog: List<AnimeItem>,
    seasonalCatalog: List<AnimeItem>,
    watchHistory: List<WatchHistoryEntity>,
    watchlist: List<WatchlistEntity>,
    heroIndex: Int,
    onSetHeroIndex: (Int) -> Unit,
    onOpenAnimeDetails: (AnimeItem) -> Unit,
    onWatchNow: (AnimeItem) -> Unit,
    onToggleWatchlist: (AnimeItem) -> Unit,
    onOpenScheduleTab: () -> Unit,
    onOpenWebReaderTab: () -> Unit,
    onOpenExploreTab: () -> Unit
) {
    val spotlightItems = catalog.take(5)
    val currentHero = spotlightItems.getOrNull(heroIndex.coerceIn(0, (spotlightItems.size - 1).coerceAtLeast(0)))

    // Auto-advance spotlight carousel every 6 seconds
    LaunchedEffect(heroIndex, spotlightItems.size) {
        if (spotlightItems.size > 1) {
            delay(6000)
            onSetHeroIndex((heroIndex + 1) % spotlightItems.size)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .testTag("home_screen_list"),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // 1. Hero Spotlight Carousel
        if (currentHero != null) {
            item {
                val isSaved = watchlist.any { it.malId == currentHero.malId }
                HeroSpotlightBanner(
                    anime = currentHero,
                    isArabic = isArabic,
                    isSaved = isSaved,
                    currentIndex = heroIndex,
                    totalCount = spotlightItems.size,
                    onSelectDot = onSetHeroIndex,
                    onWatchNow = { onWatchNow(currentHero) },
                    onToggleWatchlist = { onToggleWatchlist(currentHero) },
                    onOpenDetails = { onOpenAnimeDetails(currentHero) }
                )
            }
        }

        // 2. Flagship Feature Promo Card: Bihi Universal Web-Reader
        item {
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("home_web_reader_promo_card"),
                borderColor = CyberPurple.copy(alpha = 0.55f),
                backgroundColor = RichObsidian,
                onClick = onOpenWebReaderTab
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    ElectricCrimson.copy(alpha = 0.16f),
                                    CyberPurple.copy(alpha = 0.22f)
                                )
                            )
                        )
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(BihiPrimaryGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "قارئ مواقع الأنمي الذكي" else "Bihi Universal Web-Reader",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(CanonEmerald.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isArabic) "بدون إعلانات" else "AD-FREE",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CanonEmerald
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = if (isArabic) {
                                    "الصق رابط أي موقع أنمي خارجي لسحب الكتالوج والحلقات والسيرفرات تلقائياً داخل التطبيق!"
                                } else {
                                    "Paste any external anime site URL to extract catalogs & native streams automatically!"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // 3. Continue Watching Rail (if user has watch history)
        if (watchHistory.isNotEmpty()) {
            item {
                SectionHeaderRow(
                    title = if (isArabic) "متابعة المشاهدة (Continue Watching)" else "Continue Watching",
                    subtitle = if (isArabic) "استكمل من حيث توقفت" else "Resume from last timestamp"
                )
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(watchHistory.take(8), key = { "${it.malId}_${it.episodeNumber}" }) { history ->
                        ContinueWatchingCard(
                            history = history,
                            isArabic = isArabic,
                            onClick = {
                                val matched = catalog.find { it.malId == history.malId }
                                if (matched != null) {
                                    onWatchNow(matched)
                                }
                            }
                        )
                    }
                }
            }
        }

        // 4. Latest Episodes Rail (آخر الحلقات المضافة)
        item {
            SectionHeaderRow(
                title = if (isArabic) "آخر الحلقات المضافة" else "Latest Episodes Added",
                subtitle = if (isArabic) "تحديثات فورية بجودة 1080p FHD" else "Instant simulcast updates in 1080p",
                actionLabel = if (isArabic) "جدول المواعيد" else "Schedule",
                onActionClick = onOpenScheduleTab
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(catalog, key = { "latest_${it.malId}" }) { anime ->
                    AnimePosterCard(
                        anime = anime,
                        isArabic = isArabic,
                        showEpisodeBadge = true,
                        onClick = { onOpenAnimeDetails(anime) }
                    )
                }
            }
        }

        // 5. Seasonal Airing Rail (أنميات الموسم الحالي)
        item {
            SectionHeaderRow(
                title = if (isArabic) "أنميات الموسم الحالي" else "Seasonal Airing Now",
                subtitle = if (isArabic) "العروض المستمرة هذا الموسم" else "Simulcast series airing this season",
                actionLabel = if (isArabic) "استكشاف الكل" else "Explore All",
                onActionClick = onOpenExploreTab
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(seasonalCatalog.ifEmpty { catalog }, key = { "seasonal_${it.malId}" }) { anime ->
                    AnimePosterCard(
                        anime = anime,
                        isArabic = isArabic,
                        showEpisodeBadge = true,
                        onClick = { onOpenAnimeDetails(anime) }
                    )
                }
            }
        }

        // 6. Top Rated (الأعلى تقييماً - MAL / AniList Sync)
        item {
            val topRated = catalog.sortedByDescending { it.score }
            SectionHeaderRow(
                title = if (isArabic) "الأعلى تقييماً (MAL / AniList)" else "Top Rated (MAL / AniList Sync)",
                subtitle = if (isArabic) "أساطير الأنمي حسب تقييم الجمهور العالمي" else "All-time highest rated masterpieces"
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(topRated, key = { "top_${it.malId}" }) { anime ->
                    AnimePosterCard(
                        anime = anime,
                        isArabic = isArabic,
                        showEpisodeBadge = false,
                        onClick = { onOpenAnimeDetails(anime) }
                    )
                }
            }
        }

        // 7. Trending This Week (الأكثر شعبية هذا الأسبوع)
        item {
            val trending = catalog.sortedBy { it.popularity }
            SectionHeaderRow(
                title = if (isArabic) "الأكثر شعبية هذا الأسبوع" else "Trending This Week",
                subtitle = if (isArabic) "الأنميات الأكثر مشاهدة وتفاعلاً" else "Most streamed series right now"
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(trending, key = { "trend_${it.malId}" }) { anime ->
                    AnimePosterCard(
                        anime = anime,
                        isArabic = isArabic,
                        showEpisodeBadge = true,
                        onClick = { onOpenAnimeDetails(anime) }
                    )
                }
            }
        }
    }
}

@Composable
private fun HeroSpotlightBanner(
    anime: AnimeItem,
    isArabic: Boolean,
    isSaved: Boolean,
    currentIndex: Int,
    totalCount: Int,
    onSelectDot: (Int) -> Unit,
    onWatchNow: () -> Unit,
    onToggleWatchlist: () -> Unit,
    onOpenDetails: () -> Unit
) {
    val title = if (isArabic) anime.titleAr else anime.titleEn
    val synopsis = if (isArabic) anime.synopsisAr else anime.synopsisEn
    val genres = if (isArabic) anime.genresAr else anime.genresEn

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(390.dp)
            .clickable { onOpenDetails() }
            .testTag("hero_spotlight_banner")
    ) {
        // Backdrop Image
        AsyncImage(
            model = anime.bannerUrl.ifBlank { anime.posterUrl },
            contentDescription = title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Multi-stop Witcher Glassmorphic Gradient Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AmoledBlack.copy(alpha = 0.35f),
                            AmoledBlack.copy(alpha = 0.65f),
                            AmoledBlack.copy(alpha = 0.96f),
                            AmoledBlack
                        )
                    )
                )
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            // Top Badges: Studio, MAL Score, Status
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ElectricCrimson)
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isArabic) "🔥 اختيارات الأسبوع" else "🔥 SPOTLIGHT",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(CyberPurple.copy(alpha = 0.35f))
                        .border(1.dp, CyberPurple, RoundedCornerShape(8.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = anime.studio,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(ObsidianElevated.copy(alpha = 0.9f))
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = null,
                        tint = StarGold,
                        modifier = Modifier.size(14.dp)
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f MAL", anime.score),
                        style = MaterialTheme.typography.labelSmall,
                        color = StarGold,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.displayMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Genre Pills
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                genres.take(4).forEach { genre ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(Color.White.copy(alpha = 0.08f))
                            .border(1.dp, GlassBorder, RoundedCornerShape(50))
                            .padding(horizontal = 10.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = genre,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = synopsis,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Action Buttons: Watch Now & Add to Watchlist + Carousel Dots
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = onWatchNow,
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCrimson),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("hero_watch_now_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "شاهد الآن" else "Watch Now",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }

                    OutlinedButton(
                        onClick = onToggleWatchlist,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(
                            1.dp,
                            if (isSaved) CanonEmerald else Color.White.copy(alpha = 0.25f)
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = ObsidianElevated.copy(alpha = 0.7f)
                        ),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        modifier = Modifier.testTag("hero_watchlist_btn")
                    ) {
                        Icon(
                            imageVector = if (isSaved) Icons.Default.BookmarkAdded else Icons.Default.BookmarkAdd,
                            contentDescription = null,
                            tint = if (isSaved) CanonEmerald else TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) {
                                if (isSaved) "في مكتبتي" else "إضافة للمفضلة"
                            } else {
                                if (isSaved) "In Library" else "Watchlist"
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isSaved) CanonEmerald else TextPrimary
                        )
                    }
                }

                // Carousel Indicator Dots
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (i in 0 until totalCount) {
                        val active = i == currentIndex
                        Box(
                            modifier = Modifier
                                .height(7.dp)
                                .width(if (active) 22.dp else 7.dp)
                                .clip(CircleShape)
                                .background(if (active) ElectricCrimson else Color.White.copy(alpha = 0.28f))
                                .clickable { onSelectDot(i) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContinueWatchingCard(
    history: WatchHistoryEntity,
    isArabic: Boolean,
    onClick: () -> Unit
) {
    val progress = (history.positionSeconds.toFloat() / history.durationSeconds.coerceAtLeast(1)).coerceIn(0.08f, 0.95f)
    val minutesLeft = ((history.durationSeconds - history.positionSeconds) / 60).coerceAtLeast(1)

    GlassmorphicCard(
        modifier = Modifier.width(250.dp),
        onClick = onClick
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(ObsidianElevated),
                    contentAlignment = Alignment.Center
                ) {
                    AsyncImage(
                        model = history.posterUrl,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    Box(
                        modifier = Modifier
                            .size(26.dp)
                            .clip(CircleShape)
                            .background(ElectricCrimson.copy(alpha = 0.9f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isArabic) history.animeTitleAr else history.animeTitleEn,
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (isArabic) {
                            "الحلقة ${history.episodeNumber} • متبقي $minutesLeft د"
                        } else {
                            "EP ${history.episodeNumber} • ${minutesLeft}m left"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = ElectricCrimson
                    )
                    Text(
                        text = "${history.serverName} • ${history.quality}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary,
                        maxLines = 1
                    )
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
                trackColor = Color.White.copy(alpha = 0.12f)
            )
        }
    }
}

@Composable
fun ScheduleScreen(
    isArabic: Boolean,
    catalog: List<AnimeItem>,
    selectedDay: AnimeDayOfWeek,
    onSelectDay: (AnimeDayOfWeek) -> Unit,
    onOpenAnimeDetails: (AnimeItem) -> Unit,
    onWatchNow: (AnimeItem) -> Unit
) {
    val days = AnimeDayOfWeek.entries
    val dayAnimeList = catalog.filter { it.broadcastDay == selectedDay }
        .ifEmpty { catalog.take(4) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .testTag("schedule_screen")
    ) {
        // Header Banner
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(RichObsidian)
                .padding(horizontal = 16.dp, vertical = 14.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = ElectricCrimson
                )
                Text(
                    text = if (isArabic) "جدول مواعيد الحلقات الأسبوعي" else "Simulcast Release Schedule",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isArabic) {
                    "مواعيد عرض الحلقات بتوقيت مكة المكرمة مع عداد تنازلي مباشر للبث المتزامن"
                } else {
                    "Exact simulcast broadcast times with live airing countdowns"
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Day-by-Day Interactive Pills (السبت to الجمعة)
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(days, key = { it.name }) { day ->
                    val isSelected = day == selectedDay
                    Surface(
                        onClick = { onSelectDay(day) },
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) ElectricCrimson else ObsidianElevated,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) ElectricCrimson else GlassBorder
                        ),
                        modifier = Modifier.testTag("schedule_day_${day.key}")
                    ) {
                        Text(
                            text = if (isArabic) day.labelAr else day.labelEn,
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                        )
                    }
                }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(dayAnimeList, key = { "sched_${it.malId}" }) { anime ->
                val hours = anime.countdownMinutes / 60
                val mins = anime.countdownMinutes % 60
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onOpenAnimeDetails(anime) }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        AsyncImage(
                            model = anime.posterUrl,
                            contentDescription = anime.titleAr,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .width(76.dp)
                                .height(106.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ObsidianElevated)
                        )

                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CyberPurple.copy(alpha = 0.25f))
                                        .border(1.dp, CyberPurple, RoundedCornerShape(6.dp))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = anime.broadcastTimeJst,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFDDD6FE),
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CanonEmerald.copy(alpha = 0.16f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = null,
                                        tint = CanonEmerald,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Text(
                                        text = if (isArabic) {
                                            "بعد ${hours}س و ${mins}د"
                                        } else {
                                            "In ${hours}h ${mins}m"
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CanonEmerald,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = if (isArabic) anime.titleAr else anime.titleEn,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )

                            Text(
                                text = "${anime.studio} • ${if (isArabic) "الحلقة القادمة" else "Next EP"} ${(anime.latestEpisodeNumber + 1).coerceAtMost(anime.episodesCount)}",
                                style = MaterialTheme.typography.bodySmall,
                                color = CyberCyan
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    onClick = { onWatchNow(anime) },
                                    color = ElectricCrimson,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Bolt,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text(
                                            text = if (isArabic) "شاهد الحلقة ${anime.latestEpisodeNumber}" else "Watch EP ${anime.latestEpisodeNumber}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
