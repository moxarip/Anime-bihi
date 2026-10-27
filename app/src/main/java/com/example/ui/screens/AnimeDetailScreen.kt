package com.example.ui.screens

import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SwapVert
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.local.CommunityReviewEntity
import com.example.data.local.EpisodeStateEntity
import com.example.data.local.WatchlistEntity
import com.example.data.model.AnimeEpisode
import com.example.data.model.AnimeItem
import com.example.data.model.WatchStatus
import com.example.ui.components.CircularScoreGauge
import com.example.ui.components.GlassmorphicCard
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.CanonEmerald
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.ElectricCrimson
import com.example.ui.theme.FillerAmber
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.RichObsidian
import com.example.ui.theme.StarGold
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AnimeDetailScreen(
    anime: AnimeItem,
    episodes: List<AnimeEpisode>,
    isLoadingEpisodes: Boolean,
    isArabic: Boolean,
    watchlistEntries: List<WatchlistEntity>,
    episodeStates: List<EpisodeStateEntity>,
    reviews: List<CommunityReviewEntity>,
    showTrailerDialog: Boolean,
    onCloseDetails: () -> Unit,
    onPlayEpisode: (AnimeEpisode) -> Unit,
    onSetWatchStatus: (WatchStatus?) -> Unit,
    onToggleEpisodeWatched: (Int) -> Unit,
    onDownloadEpisode: (Int, String, Int) -> Unit,
    onDownloadBatch: () -> Unit,
    onToggleTrailer: (Boolean) -> Unit,
    onAddReview: (String, Int, String, Boolean) -> Unit,
    onLikeReview: (Int) -> Unit
) {
    BackHandler {
        onCloseDetails()
    }

    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) } // 0: Overview, 1: Episodes, 2: Reviews
    var showStatusMenu by remember { mutableStateOf(false) }

    // Episode Tab Controls
    var epSearchQuery by remember { mutableStateOf("") }
    var epAscending by remember { mutableStateOf(true) }
    var epGridView by remember { mutableStateOf(false) }

    // Review Composer State
    var reviewAuthor by remember { mutableStateOf("") }
    var reviewComment by remember { mutableStateOf("") }
    var reviewRating by remember { mutableIntStateOf(5) }
    var reviewIsSpoiler by remember { mutableStateOf(false) }
    val revealedSpoilerIds = remember { mutableStateListOf<Int>() }

    val currentWatchEntry = watchlistEntries.find { it.malId == anime.malId }
    val currentStatus = WatchStatus.entries.find { it.name == currentWatchEntry?.statusCategory }

    val watchedCountForAnime = episodeStates.count { it.malId == anime.malId && it.isWatched }
    val watchedProgress = (watchedCountForAnime.toFloat() / episodes.size.coerceAtLeast(1)).coerceIn(0f, 1f)

    if (showTrailerDialog) {
        AlertDialog(
            onDismissRequest = { onToggleTrailer(false) },
            containerColor = RichObsidian,
            title = {
                Text(
                    text = if (isArabic) "العرض الدعائي الرسمي (Official Trailer)" else "Official Anime Trailer",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(170.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(AmoledBlack),
                        contentAlignment = Alignment.Center
                    ) {
                        AsyncImage(
                            model = anime.bannerUrl.ifBlank { anime.posterUrl },
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(AmoledBlack.copy(alpha = 0.5f))
                        )
                        Button(
                            onClick = {
                                onToggleTrailer(false)
                                episodes.firstOrNull()?.let { onPlayEpisode(it) }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCrimson)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isArabic) "تشغيل في مشغل بيهي" else "Play in Bihi Player")
                        }
                    }
                    Text(
                        text = "Trailer URL: ${anime.trailerUrl}",
                        style = MaterialTheme.typography.labelSmall,
                        color = CyberCyan
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { onToggleTrailer(false) }) {
                    Text(if (isArabic) "إغلاق" else "Close", color = ElectricCrimson)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .testTag("anime_detail_screen"),
        contentPadding = PaddingValues(bottom = 100.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Rich Header with Blurred/Gradient Backdrop, Poster, Titles & Circular Score Gauge
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(330.dp)
            ) {
                AsyncImage(
                    model = anime.bannerUrl.ifBlank { anime.posterUrl },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    AmoledBlack.copy(alpha = 0.45f),
                                    AmoledBlack.copy(alpha = 0.85f),
                                    AmoledBlack
                                )
                            )
                        )
                )

                // Top Back Button
                IconButton(
                    onClick = onCloseDetails,
                    modifier = Modifier
                        .padding(16.dp)
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(ObsidianElevated.copy(alpha = 0.9f))
                        .align(Alignment.TopStart)
                        .testTag("detail_back_btn")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                // Poster + Metadata Row
                Row(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    AsyncImage(
                        model = anime.posterUrl,
                        contentDescription = anime.titleAr,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(115.dp)
                            .height(165.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .border(1.5.dp, GlassBorder, RoundedCornerShape(14.dp))
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
                                    .background(ElectricCrimson)
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isArabic) anime.statusAr else anime.statusEn,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(CyberPurple.copy(alpha = 0.35f))
                                    .padding(horizontal = 7.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = anime.studio,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = if (isArabic) anime.titleAr else anime.titleEn,
                            style = MaterialTheme.typography.headlineMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = anime.titleRomaji,
                            style = MaterialTheme.typography.bodySmall,
                            color = CyberCyan,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "${anime.type} • ${anime.year} (${anime.season}) • ${anime.episodesCount} EP • ${anime.ageRating}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }

                    CircularScoreGauge(score = anime.score)
                }
            }
        }

        // 2. Action Bar: Watch Now, Watch Status Dropdown, Trailer Modal, Batch Download, Share
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = {
                            val ep = episodes.firstOrNull() ?: AnimeEpisode(
                                malId = anime.malId,
                                number = 1,
                                titleEn = "Episode 1",
                                titleAr = "الحلقة 1",
                                airedDate = "2026-01-01"
                            )
                            onPlayEpisode(ep)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = ElectricCrimson),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("detail_watch_now_btn")
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (isArabic) "مشاهدة الآن (FHD)" else "Watch Now (FHD)",
                            style = MaterialTheme.typography.labelLarge,
                            color = Color.White
                        )
                    }

                    // Watch Status Dropdown Button
                    Box {
                        Surface(
                            onClick = { showStatusMenu = true },
                            color = if (currentStatus != null) CyberPurple else ObsidianElevated,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, GlassBorder),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("detail_status_dropdown_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Bookmark,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (currentStatus != null) {
                                        if (isArabic) currentStatus.labelAr else currentStatus.labelEn
                                    } else {
                                        if (isArabic) "حالة المشاهدة" else "Add to List"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                            }
                        }

                        DropdownMenu(
                            expanded = showStatusMenu,
                            onDismissRequest = { showStatusMenu = false },
                            modifier = Modifier.background(RichObsidian)
                        ) {
                            WatchStatus.entries.forEach { st ->
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (isArabic) st.labelAr else st.labelEn,
                                            color = if (currentStatus == st) ElectricCrimson else TextPrimary
                                        )
                                    },
                                    onClick = {
                                        onSetWatchStatus(st)
                                        showStatusMenu = false
                                    }
                                )
                            }
                            if (currentStatus != null) {
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            text = if (isArabic) "إزالة من القائمة" else "Remove from List",
                                            color = FillerAmber
                                        )
                                    },
                                    onClick = {
                                        onSetWatchStatus(null)
                                        showStatusMenu = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Secondary Action Chips: Trailer, Download Batch, Share
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ActionUtilityChip(
                        icon = Icons.Default.Movie,
                        label = if (isArabic) "العرض الدعائي" else "Trailer",
                        modifier = Modifier.weight(1f),
                        onClick = { onToggleTrailer(true) }
                    )
                    ActionUtilityChip(
                        icon = Icons.Default.CloudDownload,
                        label = if (isArabic) "تحميل دفعة" else "Batch Download",
                        modifier = Modifier.weight(1f),
                        onClick = onDownloadBatch
                    )
                    ActionUtilityChip(
                        icon = Icons.Default.Share,
                        label = if (isArabic) "مشاركة" else "Share",
                        modifier = Modifier.weight(1f),
                        onClick = {
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(
                                    Intent.EXTRA_TEXT,
                                    "شاهد أنمي ${anime.titleAr} (${anime.titleEn}) بجودة FHD على تطبيق Anime Bihi!"
                                )
                                type = "text/plain"
                            }
                            context.startActivity(Intent.createChooser(sendIntent, null))
                        }
                    )
                }
            }
        }

        // 3. Three Interactive Details Tabs: نظرة عامة (Overview) | الحلقات (Episodes) | التعليقات (Reviews)
        item {
            val detailTabs = listOf(
                0 to (if (isArabic) "نظرة عامة" else "Overview"),
                1 to (if (isArabic) "الحلقات (${episodes.size})" else "Episodes (${episodes.size})"),
                2 to (if (isArabic) "التعليقات والمراجعات" else "Community Reviews")
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(RichObsidian)
                    .border(1.dp, GlassBorder, RoundedCornerShape(14.dp))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                detailTabs.forEach { (idx, label) ->
                    val active = selectedTab == idx
                    Surface(
                        onClick = { selectedTab = idx },
                        shape = RoundedCornerShape(10.dp),
                        color = if (active) ElectricCrimson else Color.Transparent,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("detail_tab_$idx")
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (active) Color.White else TextSecondary
                            )
                        }
                    }
                }
            }
        }

        // TAB 0: OVERVIEW (نظرة عامة: القصة، الشخصيات، أنميات مرتبطة، إحصائيات المستخدمين)
        if (selectedTab == 0) {
            item {
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = if (isArabic) "قصة الأنمي (Synopsis)" else "Storyline & Synopsis",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (isArabic) anime.synopsisAr else anime.synopsisEn,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            val genres = if (isArabic) anime.genresAr else anime.genresEn
                            items(genres) { g ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(50))
                                        .background(CyberPurple.copy(alpha = 0.2f))
                                        .border(1.dp, CyberPurple.copy(alpha = 0.5f), RoundedCornerShape(50))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = g,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFDDD6FE)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Characters & Voice Actors Grid
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isArabic) "الشخصيات ومؤدو الأصوات (Characters & VA)" else "Characters & Voice Actors",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(anime.characters, key = { it.id }) { char ->
                            GlassmorphicCard(modifier = Modifier.width(200.dp)) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    AsyncImage(
                                        model = char.imageUrl,
                                        contentDescription = char.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(52.dp)
                                            .clip(CircleShape)
                                            .background(ObsidianElevated)
                                    )
                                    Column {
                                        Text(
                                            text = char.name,
                                            style = MaterialTheme.typography.labelLarge,
                                            color = TextPrimary,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = if (isArabic) char.roleAr else char.roleEn,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = ElectricCrimson
                                        )
                                        Text(
                                            text = "🎙️ ${char.voiceActor}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CyberCyan
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Related Anime (Prequel, Sequel, Side Story)
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = if (isArabic) "أنميات ذات صلة (Related Anime)" else "Related Anime (Sequel / Prequel)",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        items(anime.relatedAnime, key = { it.malId }) { rel ->
                            GlassmorphicCard(modifier = Modifier.width(210.dp)) {
                                Row(
                                    modifier = Modifier.padding(10.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    AsyncImage(
                                        model = rel.posterUrl,
                                        contentDescription = rel.titleAr,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .width(48.dp)
                                            .height(68.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                    )
                                    Column {
                                        Text(
                                            text = if (isArabic) rel.relationAr else rel.relationEn,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = CanonEmerald,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = if (isArabic) rel.titleAr else rel.title,
                                            style = MaterialTheme.typography.labelMedium,
                                            color = TextPrimary,
                                            maxLines = 2,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Anime Slayer Style User Statistics Chart
            item {
                val stats = anime.userStats
                val totalUsers = (stats.watchingCount + stats.completedCount + stats.planToWatchCount + stats.droppedCount).coerceAtLeast(1)
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isArabic) "إحصائيات وتقييمات المتابعين (Anime Slayer Stats)" else "Community Watch Statistics",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        StatDistributionBar(
                            label = if (isArabic) "يشاهدونه حالياً (${stats.watchingCount})" else "Watching (${stats.watchingCount})",
                            fraction = stats.watchingCount.toFloat() / totalUsers,
                            color = CanonEmerald
                        )
                        StatDistributionBar(
                            label = if (isArabic) "أكملوه (${stats.completedCount})" else "Completed (${stats.completedCount})",
                            fraction = stats.completedCount.toFloat() / totalUsers,
                            color = CyberPurple
                        )
                        StatDistributionBar(
                            label = if (isArabic) "يخططون لمشاهدته (${stats.planToWatchCount})" else "Plan to Watch (${stats.planToWatchCount})",
                            fraction = stats.planToWatchCount.toFloat() / totalUsers,
                            color = CyberCyan
                        )
                    }
                }
            }
        }

        // TAB 1: EPISODES (الحلقات: بحث، ترتيب، شبكة/قائمة، شارة فيلر/أصلية، وتحديد كمشاهد)
        if (selectedTab == 1) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Watched Progress Bar
                    GlassmorphicCard(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (isArabic) {
                                        "تقدم المشاهدة: $watchedCountForAnime من ${episodes.size} حلقة"
                                    } else {
                                        "Watch Progress: $watchedCountForAnime / ${episodes.size} Episodes"
                                    },
                                    style = MaterialTheme.typography.labelLarge,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "${(watchedProgress * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CanonEmerald
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { watchedProgress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(6.dp)),
                                color = CanonEmerald,
                                trackColor = Color.White.copy(alpha = 0.12f)
                            )
                        }
                    }

                    // Search & Sort / Grid-List Controls
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = epSearchQuery,
                            onValueChange = { epSearchQuery = it },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            placeholder = {
                                Text(
                                    text = if (isArabic) "ابحث برقم الحلقة أو عنوانها..." else "Search episode # or title..."
                                )
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Search, contentDescription = null, tint = ElectricCrimson)
                            },
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = ElectricCrimson,
                                unfocusedBorderColor = GlassBorder,
                                focusedContainerColor = RichObsidian,
                                unfocusedContainerColor = RichObsidian
                            ),
                            shape = RoundedCornerShape(12.dp)
                        )

                        IconButton(
                            onClick = { epAscending = !epAscending },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ObsidianElevated)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SwapVert,
                                contentDescription = "Sort Ascending/Descending",
                                tint = CyberCyan
                            )
                        }

                        IconButton(
                            onClick = { epGridView = !epGridView },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(ObsidianElevated)
                        ) {
                            Icon(
                                imageVector = if (epGridView) Icons.Default.ViewList else Icons.Default.GridView,
                                contentDescription = "Toggle Grid/List View",
                                tint = ElectricCrimson
                            )
                        }
                    }
                }
            }

            val filteredEpisodes = episodes.filter { ep ->
                epSearchQuery.isBlank() ||
                    ep.number.toString().contains(epSearchQuery) ||
                    ep.titleAr.contains(epSearchQuery, ignoreCase = true) ||
                    ep.titleEn.contains(epSearchQuery, ignoreCase = true)
            }.let { list ->
                if (epAscending) list.sortedBy { it.number } else list.sortedByDescending { it.number }
            }

            if (epGridView) {
                val chunks = filteredEpisodes.chunked(3)
                items(chunks.size, key = { "ep_grid_$it" }) { rowIdx ->
                    val rowItems = chunks[rowIdx]
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        rowItems.forEach { ep ->
                            val state = episodeStates.find { it.malId == anime.malId && it.episodeNumber == ep.number }
                            val isWatched = state?.isWatched == true
                            GlassmorphicCard(
                                modifier = Modifier.weight(1f),
                                borderColor = if (isWatched) CanonEmerald else GlassBorder,
                                onClick = { onPlayEpisode(ep) }
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Text(
                                        text = "EP ${ep.number}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (isWatched) CanonEmerald else TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (ep.isFiller) (if (isArabic) "فيلر" else "Filler") else (if (isArabic) "أصلية" else "Canon"),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (ep.isFiller) FillerAmber else CanonEmerald
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                items(filteredEpisodes, key = { "ep_list_${it.number}" }) { ep ->
                    val state = episodeStates.find { it.malId == anime.malId && it.episodeNumber == ep.number }
                    val isWatched = state?.isWatched == true
                    val isDownloaded = state?.isDownloaded == true

                    GlassmorphicCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp)
                            .testTag("episode_item_${ep.number}"),
                        borderColor = if (isWatched) CanonEmerald.copy(alpha = 0.6f) else GlassBorder,
                        onClick = { onPlayEpisode(ep) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Episode Number Box
                            Box(
                                modifier = Modifier
                                    .size(46.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(if (isWatched) CanonEmerald.copy(alpha = 0.2f) else ElectricCrimson.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${ep.number}",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isWatched) CanonEmerald else ElectricCrimson,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Filler vs Canon Badge
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(6.dp))
                                            .background(
                                                if (ep.isFiller) FillerAmber.copy(alpha = 0.2f)
                                                else CanonEmerald.copy(alpha = 0.2f)
                                            )
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = if (ep.isFiller) {
                                                if (isArabic) "حلقة فيلر (Filler)" else "Filler"
                                            } else {
                                                if (isArabic) "قصة أصلية (Canon)" else "Canon"
                                            },
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (ep.isFiller) FillerAmber else CanonEmerald,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }

                                    Text(
                                        text = "${ep.airedDate} • ${ep.durationMinutes}m",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(3.dp))

                                Text(
                                    text = if (isArabic) ep.titleAr else ep.titleEn,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }

                            // Mark as Watched Eye Toggle + Download Button
                            IconButton(
                                onClick = { onToggleEpisodeWatched(ep.number) },
                                modifier = Modifier.testTag("toggle_watched_ep_${ep.number}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RemoveRedEye,
                                    contentDescription = "Mark as Watched",
                                    tint = if (isWatched) CanonEmerald else TextSecondary
                                )
                            }

                            IconButton(
                                onClick = { onDownloadEpisode(ep.number, "1080p FHD", 340) }
                            ) {
                                Icon(
                                    imageVector = if (isDownloaded) Icons.Default.CheckCircle else Icons.Default.Download,
                                    contentDescription = "Download Episode",
                                    tint = if (isDownloaded) CyberCyan else TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // TAB 2: COMMUNITY REVIEWS (التعليقات والمراجعات مع حماية من الحرق Spoiler Protection)
        if (selectedTab == 2) {
            item {
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = if (isArabic) "أضف مراجعتك أو تعليقك" else "Write a Community Review",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )

                        OutlinedTextField(
                            value = reviewAuthor,
                            onValueChange = { reviewAuthor = it },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            placeholder = {
                                Text(if (isArabic) "اسمك المستعار (اختياري)..." else "Your nickname...")
                            },
                            shape = RoundedCornerShape(10.dp)
                        )

                        OutlinedTextField(
                            value = reviewComment,
                            onValueChange = { reviewComment = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = {
                                Text(if (isArabic) "اكتب رأيك في الأنمي أو الحلقة..." else "Share your thoughts on this anime...")
                            },
                            shape = RoundedCornerShape(10.dp)
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Checkbox(
                                    checked = reviewIsSpoiler,
                                    onCheckedChange = { reviewIsSpoiler = it }
                                )
                                Text(
                                    text = if (isArabic) "يحتوي على حرق (Spoiler)" else "Contains Spoiler",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = FillerAmber
                                )
                            }

                            Button(
                                onClick = {
                                    if (reviewComment.isNotBlank()) {
                                        onAddReview(
                                            reviewAuthor.ifBlank { if (isArabic) "متابع أنمي بيهي" else "Bihi Otaku" },
                                            reviewRating,
                                            reviewComment,
                                            reviewIsSpoiler
                                        )
                                        reviewComment = ""
                                        reviewIsSpoiler = false
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = ElectricCrimson),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = if (isArabic) "نشر التعليق" else "Post Review",
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }

            val animeReviews = reviews.filter { it.malId == anime.malId }.ifEmpty { reviews }
            items(animeReviews, key = { "rev_${it.id}" }) { rev ->
                val isSpoilerHidden = rev.isSpoiler && !revealedSpoilerIds.contains(rev.id)
                GlassmorphicCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = rev.authorName,
                                style = MaterialTheme.typography.titleSmall,
                                color = CyberCyan,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "★".repeat(rev.rating),
                                style = MaterialTheme.typography.labelMedium,
                                color = StarGold
                            )
                        }

                        if (isSpoilerHidden) {
                            Surface(
                                onClick = { revealedSpoilerIds.add(rev.id) },
                                color = FillerAmber.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(1.dp, FillerAmber.copy(alpha = 0.5f)),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.WarningAmber,
                                        contentDescription = null,
                                        tint = FillerAmber
                                    )
                                    Text(
                                        text = if (isArabic) {
                                            "تنبيه حرق للأحداث! اضغط هنا لإظهار التعليق المخفي"
                                        } else {
                                            "Spoiler Warning! Tap to reveal hidden review"
                                        },
                                        style = MaterialTheme.typography.labelMedium,
                                        color = FillerAmber,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        } else {
                            Text(
                                text = rev.comment,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            Surface(
                                onClick = { onLikeReview(rev.id) },
                                color = ObsidianElevated,
                                shape = RoundedCornerShape(50)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ThumbUp,
                                        contentDescription = "Like",
                                        tint = ElectricCrimson,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text(
                                        text = "${rev.likesCount}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextPrimary
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

@Composable
private fun ActionUtilityChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = ObsidianElevated,
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, GlassBorder),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = CyberCyan,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun StatDistributionBar(
    label: String,
    fraction: Float,
    color: Color
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.labelMedium, color = TextSecondary)
            Text(
                text = "${(fraction * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                color = color,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress = { fraction.coerceIn(0.05f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(6.dp)),
            color = color,
            trackColor = Color.White.copy(alpha = 0.1f)
        )
    }
}
