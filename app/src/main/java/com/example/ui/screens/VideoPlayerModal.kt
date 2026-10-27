package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.AspectRatio
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.Dns
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.data.model.AnimeEpisode
import com.example.data.model.StreamServer
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
import com.example.ui.viewmodel.ActivePlayerSession
import kotlinx.coroutines.delay

@Composable
fun VideoPlayerModal(
    session: ActivePlayerSession,
    isArabic: Boolean,
    onSelectServer: (StreamServer) -> Unit,
    onSelectQuality: (String) -> Unit,
    onSelectSpeed: (Float) -> Unit,
    onTogglePlayPause: () -> Unit,
    onSeekTo: (Int) -> Unit,
    onSkipIntro85s: () -> Unit,
    onPlayAdjacentEpisode: (Int) -> Unit,
    onToggleTheaterMode: () -> Unit,
    onTogglePipMode: () -> Unit,
    onDownloadEpisode: (Int, String, Int) -> Unit,
    onSelectEpisodeDirect: (AnimeEpisode) -> Unit,
    onClosePlayer: () -> Unit
) {
    var showDownloadModal by remember { mutableStateOf(false) }

    // Advance playback timer while playing
    LaunchedEffect(session.isPlaying, session.currentSeconds, session.playbackSpeed) {
        if (session.isPlaying && session.currentSeconds < session.totalSeconds) {
            val intervalMs = (1000L / session.playbackSpeed).toLong().coerceAtLeast(250L)
            delay(intervalMs)
            onSeekTo(session.currentSeconds + 1)
        }
    }

    // Picture-in-Picture (PiP) Floating Mini-Player Mode
    if (session.isPipMinimized) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 90.dp, start = 16.dp, end = 16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("pip_mini_player"),
                borderColor = ElectricCrimson,
                backgroundColor = RichObsidian,
                onClick = onTogglePipMode
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .width(96.dp)
                            .height(56.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(AmoledBlack)
                    ) {
                        AsyncImage(
                            model = session.anime.bannerUrl.ifBlank { session.anime.posterUrl },
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = if (isArabic) session.anime.titleAr else session.anime.titleEn,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "EP ${session.episode.number} • ${session.selectedServer.name}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberCyan
                        )
                    }
                    IconButton(onClick = onTogglePlayPause) {
                        Icon(
                            imageVector = if (session.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play/Pause",
                            tint = ElectricCrimson
                        )
                    }
                    IconButton(onClick = onClosePlayer) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary
                        )
                    }
                }
            }
        }
        return
    }

    BackHandler {
        onClosePlayer()
    }

    if (showDownloadModal) {
        val downloadOptions = listOf(
            Triple("1080p FHD • Blu-Ray SoftSub", 385, "سيرفر أنمي بيهي المباشر"),
            Triple("720p HD • Web-DL Fast", 215, "سيرفر أنمي بيهي المباشر"),
            Triple("480p SD • Data Saver", 110, "سيرفر أنمي بيهي المباشر")
        )
        AlertDialog(
            onDismissRequest = { showDownloadModal = false },
            containerColor = RichObsidian,
            title = {
                Text(
                    text = if (isArabic) {
                        "مدير التحميل المباشر - الحلقة ${session.episode.number}"
                    } else {
                        "Direct Episode Download - EP ${session.episode.number}"
                    },
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    downloadOptions.forEach { (qualityLabel, sizeMb, serverDesc) ->
                        GlassmorphicCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = CyberCyan.copy(alpha = 0.5f),
                            onClick = {
                                onDownloadEpisode(session.episode.number, qualityLabel.substringBefore(" •"), sizeMb)
                                showDownloadModal = false
                            }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = qualityLabel,
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = serverDesc,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CanonEmerald
                                    )
                                }
                                Text(
                                    text = "$sizeMb MB ⬇",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CyberCyan
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showDownloadModal = false }) {
                    Text(if (isArabic) "إغلاق" else "Cancel", color = ElectricCrimson)
                }
            }
        )
    }

    val speeds = listOf(0.5f, 1.0f, 1.25f, 1.5f, 2.0f)
    val curMin = (session.currentSeconds / 60).toString().padStart(2, '0')
    val curSec = (session.currentSeconds % 60).toString().padStart(2, '0')
    val totMin = (session.totalSeconds / 60).toString().padStart(2, '0')
    val totSec = (session.totalSeconds % 60).toString().padStart(2, '0')

    val activeSubtitleAr = when ((session.currentSeconds / 6) % 4) {
        0 -> "« لن أتراجع أبداً... حتى لو وقف العالم بأسره في طريقي! »"
        1 -> "« طاقة الظلال تتصاعد بسرعة تتجاوز قراءات النقابة! »"
        2 -> "« تذكر دائماً سبب حملك لهذا النصل في اللحظة الحاسمة. »"
        else -> "« استعدوا جميعاً، البوابة القرمزية على وشك الانفتاح الآن! »"
    }
    val activeSubtitleEn = when ((session.currentSeconds / 6) % 4) {
        0 -> "\"I will never step back... even if the entire world stands in my way!\""
        1 -> "\"Shadow mana output is surging beyond the Guild's S-Rank sensors!\""
        2 -> "\"Remember why you unsheathed that blade at the decisive moment.\""
        else -> "\"Brace yourselves, the Crimson Gate is opening right now!\""
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .testTag("video_player_modal"),
        contentPadding = PaddingValues(bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Video Viewport Canvas + Interactive Overlay Controls
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (session.isTheaterMode) 320.dp else 245.dp)
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = session.anime.bannerUrl.ifBlank { session.anime.posterUrl },
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )

                // Animated Dynamic Energy / Scanline Effect on Video Canvas
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    drawRect(
                        brush = Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.55f),
                                Color.Transparent,
                                Color.Black.copy(alpha = 0.82f)
                            )
                        )
                    )
                    val pulseX = ((session.currentSeconds % 30) / 30f) * w
                    drawCircle(
                        color = ElectricCrimson.copy(alpha = 0.16f),
                        radius = h * 0.4f,
                        center = Offset(pulseX, h * 0.5f)
                    )
                }

                // Top Player Bar: Close, Episode Title, Server Status, PiP & Theater Toggles
                Row(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        IconButton(
                            onClick = onClosePlayer,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AmoledBlack.copy(alpha = 0.75f))
                                .testTag("close_player_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Player",
                                tint = Color.White
                            )
                        }
                        Column {
                            Text(
                                text = if (isArabic) session.anime.titleAr else session.anime.titleEn,
                                style = MaterialTheme.typography.titleSmall,
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "EP ${session.episode.number} • ${session.selectedServer.name} (${session.selectedQuality})",
                                style = MaterialTheme.typography.labelSmall,
                                color = CanonEmerald
                            )
                        }
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        IconButton(
                            onClick = onTogglePipMode,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AmoledBlack.copy(alpha = 0.75f))
                                .testTag("pip_toggle_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureInPictureAlt,
                                contentDescription = "Picture in Picture",
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        IconButton(
                            onClick = onToggleTheaterMode,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(AmoledBlack.copy(alpha = 0.75f))
                        ) {
                            Icon(
                                imageVector = Icons.Default.AspectRatio,
                                contentDescription = "Theater Mode",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // Center Transport Controls: Prev Episode, Play/Pause, Next Episode
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { onPlayAdjacentEpisode(-1) },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AmoledBlack.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipPrevious,
                            contentDescription = "Previous Episode",
                            tint = Color.White
                        )
                    }

                    IconButton(
                        onClick = onTogglePlayPause,
                        modifier = Modifier
                            .size(62.dp)
                            .clip(CircleShape)
                            .background(ElectricCrimson)
                            .testTag("player_play_pause_btn")
                    ) {
                        Icon(
                            imageVector = if (session.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = "Play or Pause",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    IconButton(
                        onClick = { onPlayAdjacentEpisode(1) },
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(AmoledBlack.copy(alpha = 0.7f))
                    ) {
                        Icon(
                            imageVector = Icons.Default.SkipNext,
                            contentDescription = "Next Episode",
                            tint = Color.White
                        )
                    }
                }

                // Bottom Subtitle Overlay + Skip Intro (+85s) + Timeline Scrubber
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(8.dp))
                                .background(AmoledBlack.copy(alpha = 0.78f))
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = if (isArabic) activeSubtitleAr else activeSubtitleEn,
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Skip Intro / Outro (+85s) Button
                        Surface(
                            onClick = onSkipIntro85s,
                            color = ElectricCrimson,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("skip_intro_85s_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.FastForward,
                                    contentDescription = null,
                                    tint = Color.White,
                                    modifier = Modifier.size(14.dp)
                                )
                                Text(
                                    text = if (isArabic) "تخطي المقدمة +85ث" else "Skip Intro +85s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "$curMin:$curSec",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White
                        )
                        Slider(
                            value = session.currentSeconds.toFloat(),
                            onValueChange = { onSeekTo(it.toInt()) },
                            valueRange = 0f..session.totalSeconds.toFloat(),
                            modifier = Modifier.weight(1f),
                            colors = SliderDefaults.colors(
                                thumbColor = ElectricCrimson,
                                activeTrackColor = ElectricCrimson,
                                inactiveTrackColor = Color.White.copy(alpha = 0.25f)
                            )
                        )
                        Text(
                            text = "$totMin:$totSec",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }
            }
        }

        // 2. Multi-Server Switcher Bar (Bihi Native, Scraped Direct, FHD Server, Mega, Drive, MP4Upload)
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Dns,
                                contentDescription = null,
                                tint = ElectricCrimson,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = if (isArabic) "سيرفرات المشاهدة المتعددة" else "Multi-Server Stream Switcher",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Text(
                            text = "${session.selectedServer.latencyMs}ms Ping",
                            style = MaterialTheme.typography.labelSmall,
                            color = CanonEmerald
                        )
                    }

                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(session.servers, key = { it.id }) { srv ->
                            val isSelected = srv.id == session.selectedServer.id
                            Surface(
                                onClick = { onSelectServer(srv) },
                                shape = RoundedCornerShape(12.dp),
                                color = if (isSelected) ElectricCrimson else ObsidianElevated,
                                border = BorderStroke(1.dp, if (isSelected) ElectricCrimson else GlassBorder),
                                modifier = Modifier.testTag("server_chip_${srv.id}")
                            ) {
                                Column(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                ) {
                                    Text(
                                        text = srv.name,
                                        style = MaterialTheme.typography.labelLarge,
                                        color = Color.White
                                    )
                                    Text(
                                        text = srv.badge,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isSelected) Color.White.copy(alpha = 0.85f) else CyberCyan
                                    )
                                }
                            }
                        }
                    }

                    // Quality Pills (1080p, 720p, 480p) + Playback Speed (0.5x to 2.0x) + Direct Download Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.HighQuality,
                                contentDescription = null,
                                tint = CyberCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            session.selectedServer.qualities.forEach { q ->
                                val active = q == session.selectedQuality
                                Surface(
                                    onClick = { onSelectQuality(q) },
                                    color = if (active) CyberPurple else ObsidianElevated,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = q.substringBefore(" "),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color.White,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 5.dp)
                                    )
                                }
                            }
                        }

                        Button(
                            onClick = { showDownloadModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = ObsidianElevated),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("open_download_modal_btn")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudDownload,
                                contentDescription = null,
                                tint = CanonEmerald,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (isArabic) "تحميل الحلقة" else "Download EP",
                                style = MaterialTheme.typography.labelMedium,
                                color = TextPrimary
                            )
                        }
                    }

                    // Playback Speed Bar (0.5x to 2.0x)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Speed,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = if (isArabic) "سرعة التشغيل:" else "Speed:",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        speeds.forEach { spd ->
                            val active = session.playbackSpeed == spd
                            Surface(
                                onClick = { onSelectSpeed(spd) },
                                color = if (active) ElectricCrimson else ObsidianElevated,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = "${spd}x",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Color.White,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // 3. Quick Episode Switcher Grid inside Player
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = if (isArabic) "التنقل السريع بين الحلقات" else "Quick Episode Jump",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(session.allEpisodes, key = { "pl_ep_${it.number}" }) { ep ->
                        val active = ep.number == session.episode.number
                        Surface(
                            onClick = { onSelectEpisodeDirect(ep) },
                            shape = RoundedCornerShape(10.dp),
                            color = if (active) ElectricCrimson else RichObsidian,
                            border = BorderStroke(1.dp, if (active) ElectricCrimson else GlassBorder)
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "EP ${ep.number}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                                Text(
                                    text = if (ep.isFiller) (if (isArabic) "فيلر" else "Filler") else "Canon",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (active) Color.White else CyberCyan
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
