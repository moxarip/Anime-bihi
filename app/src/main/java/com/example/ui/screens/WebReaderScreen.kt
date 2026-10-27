package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.local.ScraperSourceEntity
import com.example.data.model.AnimeItem
import com.example.data.model.ScrapedSourceCatalog
import com.example.data.model.ScraperStepLog
import com.example.ui.components.AnimePosterCard
import com.example.ui.components.GlassmorphicCard
import com.example.ui.components.SectionHeaderRow
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.BihiPrimaryGradient
import com.example.ui.theme.CanonEmerald
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberPurple
import com.example.ui.theme.ElectricCrimson
import com.example.ui.theme.FillerAmber
import com.example.ui.theme.GlassBorder
import com.example.ui.theme.ObsidianElevated
import com.example.ui.theme.RichObsidian
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun WebReaderScreen(
    isArabic: Boolean,
    urlInput: String,
    cardSelector: String,
    titleSelector: String,
    posterSelector: String,
    episodeSelector: String,
    iframeSelector: String,
    isScraping: Boolean,
    scraperProgress: Float,
    scraperLogs: List<ScraperStepLog>,
    savedSources: List<ScraperSourceEntity>,
    activeScrapedCatalog: ScrapedSourceCatalog?,
    scrapedSearchQuery: String,
    onUpdateUrl: (String) -> Unit,
    onUpdateSelectors: (String, String, String, String, String) -> Unit,
    onRunScraper: (String?) -> Unit,
    onOpenSavedSource: (ScraperSourceEntity) -> Unit,
    onDeleteSavedSource: (Int) -> Unit,
    onCloseScrapedCatalog: () -> Unit,
    onUpdateScrapedSearch: (String) -> Unit,
    onOpenAnimeDetails: (AnimeItem) -> Unit,
    onStreamAnimeDirect: (AnimeItem) -> Unit
) {
    var showSelectorMapper by remember { mutableStateOf(false) }

    // If user opened a scraped source's Native Ad-Free Catalog, allow Back button to return to Web-Reader Hub
    if (activeScrapedCatalog != null && !isScraping) {
        BackHandler {
            onCloseScrapedCatalog()
        }
    }

    val quickSampleUrls = listOf(
        "https://witanime.cyou" to "WitAnime",
        "https://anime4up.rest" to "Anime4Up",
        "https://gogoanime3.co" to "GogoAnime",
        "https://animerco.org" to "Animerco",
        "https://xsanime.com" to "XSAnime"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(AmoledBlack)
            .testTag("web_reader_screen"),
        contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 100.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Flagship Hero Header
        item {
            GlassmorphicCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = CyberPurple.copy(alpha = 0.6f),
                backgroundColor = RichObsidian
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.linearGradient(
                                listOf(
                                    ElectricCrimson.copy(alpha = 0.16f),
                                    CyberPurple.copy(alpha = 0.22f),
                                    RichObsidian
                                )
                            )
                        )
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(44.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(BihiPrimaryGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Radar,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = if (isArabic) {
                                    "قارئ وساحب مواقع الأنمي الذكي"
                                } else {
                                    "Bihi Universal Web-Reader Engine"
                                },
                                style = MaterialTheme.typography.headlineMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (isArabic) {
                                    "محرك Tachiyomi/Mihon لمواقع الأنمي • سحب تلقائي للكتالوج والسيرفرات بدون إعلانات"
                                } else {
                                    "Tachiyomi/Mihon-style URL Scraper • Auto DOM Parser & Native Stream Extractor"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // URL Input Field
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = onUpdateUrl,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("scraper_url_input"),
                        singleLine = true,
                        label = {
                            Text(
                                text = if (isArabic) "إضافة موقع أنمي جديد (Add Anime Site URL)" else "Add Anime Site URL (https://...)"
                            )
                        },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Public,
                                contentDescription = null,
                                tint = ElectricCrimson
                            )
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ElectricCrimson,
                            unfocusedBorderColor = GlassBorder,
                            focusedContainerColor = AmoledBlack.copy(alpha = 0.7f),
                            unfocusedContainerColor = AmoledBlack.copy(alpha = 0.5f),
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Quick Test URL Pills
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(quickSampleUrls, key = { it.first }) { (sampleUrl, label) ->
                            Surface(
                                onClick = {
                                    onUpdateUrl(sampleUrl)
                                    onRunScraper(sampleUrl)
                                },
                                shape = RoundedCornerShape(50),
                                color = ObsidianElevated,
                                border = BorderStroke(1.dp, GlassBorder)
                            ) {
                                Text(
                                    text = "⚡ $label",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CyberCyan,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Primary Scrape Button + Custom CSS Selector Config Toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onRunScraper(null) },
                            enabled = !isScraping && urlInput.isNotBlank(),
                            colors = ButtonDefaults.buttonColors(containerColor = ElectricCrimson),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("run_scraper_btn")
                        ) {
                            if (isScraping) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(18.dp),
                                    color = Color.White,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = if (isArabic) "جاري سحب وتحليل DOM..." else "Scraping DOM...",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.Bolt,
                                    contentDescription = null,
                                    tint = Color.White
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (isArabic) "سحب وتحويل لواجهة أصلية" else "Extract & Render Native Catalog",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = Color.White
                                )
                            }
                        }

                        Surface(
                            onClick = { showSelectorMapper = !showSelectorMapper },
                            shape = RoundedCornerShape(12.dp),
                            color = if (showSelectorMapper) CyberPurple else ObsidianElevated,
                            border = BorderStroke(1.dp, CyberPurple.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .height(48.dp)
                                .testTag("toggle_css_mapper_btn")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Tune,
                                    contentDescription = "CSS Selector Config",
                                    tint = Color.White,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isArabic) "CSS متقدم" else "CSS Mapper",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    // 2. Custom CSS Selector Mapper Drawer (الوضع المتقدم لتخصيص السحب)
                    AnimatedVisibility(visible = showSelectorMapper) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 14.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(AmoledBlack.copy(alpha = 0.85f))
                                .border(1.dp, CyberPurple.copy(alpha = 0.45f), RoundedCornerShape(12.dp))
                                .padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Code,
                                    contentDescription = null,
                                    tint = CyberCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isArabic) {
                                        "تخصيص محددات CSS للمواقع المستعصية (Custom Selector Profile)"
                                    } else {
                                        "Custom CSS Selector Mapper (Reusable Site Profile)"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = CyberCyan,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            SelectorConfigField(
                                label = "Card Selector (حاوية البطاقة)",
                                value = cardSelector,
                                onValueChange = {
                                    onUpdateSelectors(it, titleSelector, posterSelector, episodeSelector, iframeSelector)
                                }
                            )
                            SelectorConfigField(
                                label = "Title Selector (عنوان الأنمي)",
                                value = titleSelector,
                                onValueChange = {
                                    onUpdateSelectors(cardSelector, it, posterSelector, episodeSelector, iframeSelector)
                                }
                            )
                            SelectorConfigField(
                                label = "Poster Selector (صورة البوستر)",
                                value = posterSelector,
                                onValueChange = {
                                    onUpdateSelectors(cardSelector, titleSelector, it, episodeSelector, iframeSelector)
                                }
                            )
                            SelectorConfigField(
                                label = "Episode List Selector (قائمة الحلقات)",
                                value = episodeSelector,
                                onValueChange = {
                                    onUpdateSelectors(cardSelector, titleSelector, posterSelector, it, iframeSelector)
                                }
                            )
                            SelectorConfigField(
                                label = "Video Stream / Iframe Selector (صائد السيرفرات)",
                                value = iframeSelector,
                                onValueChange = {
                                    onUpdateSelectors(cardSelector, titleSelector, posterSelector, episodeSelector, it)
                                }
                            )
                        }
                    }
                }
            }
        }

        // 3. Visual Scraper Simulator / Live Step-by-Step Debugger
        if (isScraping || scraperLogs.isNotEmpty()) {
            item {
                GlassmorphicCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = CyberCyan.copy(alpha = 0.45f),
                    backgroundColor = RichObsidian
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
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
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = null,
                                    tint = CanonEmerald,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isArabic) {
                                        "سجل محرك السحب الذكي (Live DOM Extraction Debugger)"
                                    } else {
                                        "Live DOM Extraction & Ad-Stripper Pipeline"
                                    },
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "${(scraperProgress * 100).toInt()}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = CanonEmerald
                            )
                        }

                        LinearProgressIndicator(
                            progress = { scraperProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(6.dp)),
                            color = CanonEmerald,
                            trackColor = Color.White.copy(alpha = 0.1f)
                        )

                        scraperLogs.forEach { step ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(AmoledBlack.copy(alpha = 0.6f))
                                    .padding(horizontal = 10.dp, vertical = 7.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = when {
                                        step.isCompleted -> CanonEmerald
                                        step.isActive -> FillerAmber
                                        else -> TextSecondary.copy(alpha = 0.4f)
                                    },
                                    modifier = Modifier.size(16.dp)
                                )
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = if (isArabic) step.titleAr else step.titleEn,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (step.isCompleted || step.isActive) TextPrimary else TextSecondary,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = step.detail,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = CyberCyan
                                    )
                                }
                                Text(
                                    text = "${step.durationMs}ms",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // 4. If a Scraped Catalog is active, render its 100% Native, Ad-Free Catalog View!
        if (activeScrapedCatalog != null) {
            item {
                ScrapedNativeCatalogHeader(
                    catalog = activeScrapedCatalog,
                    isArabic = isArabic,
                    searchQuery = scrapedSearchQuery,
                    onUpdateSearch = onUpdateScrapedSearch,
                    onCloseCatalog = onCloseScrapedCatalog
                )
            }

            val filteredItems = activeScrapedCatalog.extractedItems.filter {
                scrapedSearchQuery.isBlank() ||
                    it.titleAr.contains(scrapedSearchQuery, ignoreCase = true) ||
                    it.titleEn.contains(scrapedSearchQuery, ignoreCase = true)
            }

            val rows = filteredItems.chunked(2)
            items(rows.size, key = { idx -> "scraped_row_$idx" }) { rowIdx ->
                val pair = rows[rowIdx]
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    pair.forEach { anime ->
                        Box(modifier = Modifier.weight(1f)) {
                            ScrapedCatalogAnimeCard(
                                anime = anime,
                                isArabic = isArabic,
                                onOpenDetails = { onOpenAnimeDetails(anime) },
                                onStreamDirect = { onStreamAnimeDirect(anime) }
                            )
                        }
                    }
                    if (pair.size == 1) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }

        // 5. Saved Sources Hub (إدارة المصادر المضافة + 3 Pre-loaded Scraper Profiles)
        item {
            SectionHeaderRow(
                title = if (isArabic) "إدارة المصادر المضافة (${savedSources.size})" else "Saved Custom Sources Hub (${savedSources.size})",
                subtitle = if (isArabic) {
                    "اضغط على أي موقع لفتح كتالوجه الأصلي الخالي من الإعلانات فوراً"
                } else {
                    "Tap any saved source to browse & stream its catalog natively"
                }
            )
        }

        items(savedSources, key = { "source_${it.id}" }) { source ->
            val isCurrentlyOpen = activeScrapedCatalog?.domain == source.domain
            GlassmorphicCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("saved_source_card_${source.id}"),
                borderColor = if (isCurrentlyOpen) ElectricCrimson else GlassBorder,
                backgroundColor = if (isCurrentlyOpen) ObsidianElevated else RichObsidian,
                onClick = { onOpenSavedSource(source) }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Domain Monogram Avatar
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(BihiPrimaryGradient),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = source.domain.take(2).uppercase(),
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = source.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold
                            )
                            if (source.isPreloaded) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(CyberPurple.copy(alpha = 0.25f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = if (isArabic) "مُعد مسبقاً" else "PRE-LOADED",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFFDDD6FE)
                                    )
                                }
                            }
                        }

                        Text(
                            text = "${source.domain} • Selector: ${source.cardSelector}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🟢 ${source.statusText}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CanonEmerald
                            )
                            Text(
                                text = "🛡️ ${source.adsStripped} ${if (isArabic) "إعلان محجوب" else "ads stripped"}",
                                style = MaterialTheme.typography.labelSmall,
                                color = CyberCyan
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            onClick = { onOpenSavedSource(source) },
                            color = ElectricCrimson,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = if (isArabic) "تصفح" else "Browse",
                                style = MaterialTheme.typography.labelMedium,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                        if (!source.isPreloaded) {
                            IconButton(onClick = { onDeleteSavedSource(source.id) }) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "Delete Source",
                                    tint = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SelectorConfigField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = CyberCyan,
            unfocusedBorderColor = GlassBorder,
            focusedTextColor = TextPrimary,
            unfocusedTextColor = TextPrimary
        ),
        shape = RoundedCornerShape(8.dp)
    )
}

@Composable
private fun ScrapedNativeCatalogHeader(
    catalog: ScrapedSourceCatalog,
    isArabic: Boolean,
    searchQuery: String,
    onUpdateSearch: (String) -> Unit,
    onCloseCatalog: () -> Unit
) {
    GlassmorphicCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = CanonEmerald.copy(alpha = 0.55f),
        backgroundColor = ObsidianElevated
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onCloseCatalog,
                        modifier = Modifier
                            .size(34.dp)
                            .clip(CircleShape)
                            .background(AmoledBlack)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Close Catalog",
                            tint = TextPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Column {
                        Text(
                            text = if (isArabic) {
                                "كتالوج ${catalog.sourceName} الأصلي"
                            } else {
                                "${catalog.sourceName} Native Catalog"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${catalog.domain} • ${catalog.extractedItems.size} ${if (isArabic) "أنمي مسحوب" else "Extracted Series"} • 🛡️ ${catalog.adsBlockedCount} ${if (isArabic) "إعلانات محذوفة" else "Ads Blocked"}",
                            style = MaterialTheme.typography.labelSmall,
                            color = CanonEmerald
                        )
                    }
                }
            }

            OutlinedTextField(
                value = searchQuery,
                onValueChange = onUpdateSearch,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = {
                    Text(
                        text = if (isArabic) {
                            "ابحث داخل كتالوج ${catalog.domain} بدون إعلانات..."
                        } else {
                            "Search inside ${catalog.domain} ad-free catalog..."
                        }
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = CyberCyan
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CanonEmerald,
                    unfocusedBorderColor = GlassBorder,
                    focusedContainerColor = AmoledBlack,
                    unfocusedContainerColor = AmoledBlack
                ),
                shape = RoundedCornerShape(10.dp)
            )
        }
    }
}

@Composable
private fun ScrapedCatalogAnimeCard(
    anime: AnimeItem,
    isArabic: Boolean,
    onOpenDetails: () -> Unit,
    onStreamDirect: () -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        AnimePosterCard(
            anime = anime,
            isArabic = isArabic,
            showEpisodeBadge = true,
            width = 180.dp,
            onClick = onOpenDetails
        )
        Spacer(modifier = Modifier.height(6.dp))
        Surface(
            onClick = onStreamDirect,
            color = ElectricCrimson.copy(alpha = 0.2f),
            shape = RoundedCornerShape(8.dp),
            border = BorderStroke(1.dp, ElectricCrimson.copy(alpha = 0.6f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(vertical = 6.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = ElectricCrimson,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (isArabic) "تشغيل السيرفر المسحوب" else "Stream Scraped M3U8",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
