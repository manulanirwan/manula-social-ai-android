package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.*
import com.example.engine.HashtagEngine
import com.example.rules.PlatformRulesConfig
import com.example.ui.components.*
import com.example.ui.theme.AccentTertiary
import com.example.ui.theme.AccentWarning
import com.example.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: MainViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val socialPkg = uiState.generatedPackage ?: return

    val currentVersionPkg = when (uiState.selectedVariation) {
        VariationType.A -> socialPkg.versionA
        VariationType.B -> socialPkg.versionB
        VariationType.C -> socialPkg.versionC
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Top Bar inside Result Screen
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.navigateBackToHome() },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.ArrowBack,
                            contentDescription = "Back",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "Content Package",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${socialPkg.selectedPlatforms.size} platforms generated",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            val fullPackageText = buildFullPackageExport(socialPkg, currentVersionPkg)
                            viewModel.shareText(fullPackageText, "Share Full Social Package")
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share Full Package",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    IconButton(
                        onClick = {
                            val fullPackageText = buildFullPackageExport(socialPkg, currentVersionPkg)
                            viewModel.copyToClipboard(fullPackageText, "Full Package")
                        },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy Full Package",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
        }

        // Variation Selector (Version A, B, C)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    .padding(3.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                VariationType.values().forEach { variation ->
                    val isSelected = uiState.selectedVariation == variation
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
                        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant) else null,
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.setVariation(variation) }
                            .padding(vertical = 4.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = variation.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = variation.subtitle.take(16),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontSize = 9.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        // Platform Tab Selector (All, YouTube, TikTok, Instagram, Threads, LinkedIn, Facebook, X)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // "All" tab
                val isAllSelected = uiState.activeResultPlatform == null
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isAllSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isAllSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier.clickable { viewModel.setResultPlatformTab(null) }
                ) {
                    Text(
                        text = "All Platforms",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = if (isAllSelected) FontWeight.Bold else FontWeight.Normal,
                        color = if (isAllSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }

                // Individual Platforms
                socialPkg.selectedPlatforms.forEach { platform ->
                    val isSelected = uiState.activeResultPlatform == platform
                    val platformColor = getPlatformColor(platform)

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.surfaceVariant else MaterialTheme.colorScheme.surface,
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) platformColor else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier.clickable { viewModel.setResultPlatformTab(platform) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = getPlatformIcon(platform),
                                contentDescription = platform.displayName,
                                tint = platformColor,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = platform.displayName,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Quality Indicator Card
        item {
            QualityIndicatorCard(
                qualityScore = socialPkg.qualityScore,
                hasViolations = socialPkg.aiSuggestions.any { it.type == SuggestionType.LIMIT_EXCEEDED }
            )
        }

        // Body Content: If "All" selected -> Overview Cards, Else -> Full Platform Grouped Card
        if (uiState.activeResultPlatform == null) {
            // ALL PLATFORMS COMPACT OVERVIEW
            items(socialPkg.selectedPlatforms.toList()) { platform ->
                AllPlatformsOverviewCard(
                    platform = platform,
                    pkg = currentVersionPkg,
                    onOpenPlatform = { viewModel.setResultPlatformTab(platform) },
                    onCopyContent = { text -> viewModel.copyToClipboard(text, "${platform.displayName} content") },
                    onShareContent = { text -> viewModel.shareText(text, "Share ${platform.displayName}") }
                )
            }
        } else {
            // SINGLE PLATFORM DETAILED VIEW
            val platform = uiState.activeResultPlatform!!
            item {
                DetailedPlatformCard(
                    platform = platform,
                    pkg = currentVersionPkg,
                    originalIdea = socialPkg.originalIdea,
                    viewModel = viewModel
                )
            }
        }
    }

    // Modal Sheet / Dialogs
    uiState.editingField?.let { editContext ->
        EditContentDialog(
            fieldLabel = "${editContext.platform.displayName} ${editContext.fieldName}",
            initialContent = editContext.currentContent,
            maxLimit = editContext.maxLimit,
            onDismiss = { viewModel.closeEditField() },
            onSave = { updatedText ->
                editContext.updateAction(updatedText)
                viewModel.closeEditField()
                viewModel.showToast("Saved changes!")
            }
        )
    }

    if (uiState.regeneratingPlatform != null) {
        RegenerateBottomSheet(
            platformName = uiState.regeneratingPlatform!!.displayName,
            onDismiss = { viewModel.closeRegenerateSheet() },
            onRegenerate = { instruction ->
                viewModel.regeneratePlatformContent(instruction)
            }
        )
    }
}

@Composable
fun AllPlatformsOverviewCard(
    platform: SocialPlatform,
    pkg: PlatformPackage,
    onOpenPlatform: () -> Unit,
    onCopyContent: (String) -> Unit,
    onShareContent: (String) -> Unit
) {
    val platformColor = getPlatformColor(platform)
    val rule = PlatformRulesConfig.getRule(platform)

    val (titleText, bodyText, charCount, maxChars) = when (platform) {
        SocialPlatform.YOUTUBE -> Tuple4(pkg.youtube.title, pkg.youtube.description, pkg.youtube.title.length, rule.fields["title"]?.maxChars ?: 100)
        SocialPlatform.TIKTOK -> Tuple4(pkg.tiktok.hookText, pkg.tiktok.caption, pkg.tiktok.caption.length, rule.fields["caption"]?.maxChars ?: 2200)
        SocialPlatform.INSTAGRAM -> Tuple4(pkg.instagram.firstLineHook, pkg.instagram.caption, pkg.instagram.caption.length, rule.fields["caption"]?.maxChars ?: 2200)
        SocialPlatform.THREADS -> Tuple4("Threads Post", pkg.threads.primaryPost, pkg.threads.primaryPost.length, rule.fields["post"]?.maxChars ?: 500)
        SocialPlatform.LINKEDIN -> Tuple4(pkg.linkedin.hook, pkg.linkedin.mainPost, pkg.linkedin.mainPost.length, rule.fields["post"]?.maxChars ?: 3000)
        SocialPlatform.FACEBOOK -> Tuple4("Facebook Post", pkg.facebook.mainPost, pkg.facebook.mainPost.length, rule.fields["post"]?.maxChars ?: 63206)
        SocialPlatform.X -> Tuple4("X Post", pkg.x.standardPost, pkg.x.standardPost.length, rule.fields["post"]?.maxChars ?: 280)
    }

    val isOverLimit = charCount > maxChars

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(platformColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getPlatformIcon(platform),
                            contentDescription = platform.displayName,
                            tint = platformColor,
                            modifier = Modifier.size(14.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = platform.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "$charCount / $maxChars",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isOverLimit) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = if (isOverLimit) MaterialTheme.colorScheme.error.copy(alpha = 0.15f) else AccentTertiary.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = if (isOverLimit) "Over limit" else "Ready",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isOverLimit) MaterialTheme.colorScheme.error else AccentTertiary,
                            fontSize = 10.sp,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (titleText.isNotBlank()) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = bodyText,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextButton(
                    onClick = onOpenPlatform,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(30.dp)
                ) {
                    Text(
                        text = "View & Edit Details →",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                Row {
                    IconButton(
                        onClick = { onCopyContent("$titleText\n\n$bodyText") },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.ContentCopy,
                            contentDescription = "Copy",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    IconButton(
                        onClick = { onShareContent("$titleText\n\n$bodyText") },
                        modifier = Modifier.size(30.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Outlined.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DetailedPlatformCard(
    platform: SocialPlatform,
    pkg: PlatformPackage,
    originalIdea: String,
    viewModel: MainViewModel
) {
    val platformColor = getPlatformColor(platform)
    val rule = PlatformRulesConfig.getRule(platform)

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surface,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Platform Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(platformColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getPlatformIcon(platform),
                            contentDescription = platform.displayName,
                            tint = platformColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = platform.displayName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = rule.coreToneSummary,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Favorite Icon
                IconButton(
                    onClick = {
                        val contentToSave = getPlatformFullString(platform, pkg)
                        viewModel.saveToFavorites(
                            title = "${platform.displayName}: ${originalIdea.take(30)}",
                            content = contentToSave,
                            category = "FULL_PACKAGE",
                            platform = platform.name,
                            sourceIdea = originalIdea
                        )
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.StarOutline,
                        contentDescription = "Favorite",
                        tint = AccentWarning,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Specific Platform Fields
            when (platform) {
                SocialPlatform.YOUTUBE -> {
                    // Title Field
                    ContentFieldGroup(
                        label = "TITLE",
                        content = pkg.youtube.title,
                        maxLimit = 100,
                        onCopy = { viewModel.copyToClipboard(pkg.youtube.title, "YouTube Title") },
                        onEdit = {
                            viewModel.openEditField(platform, "title", pkg.youtube.title, 100) { newT ->
                                viewModel.updatePlatformField(platform, "title", newT)
                            }
                        },
                        onFixToFit = { viewModel.fixTextToFit(platform, "title", pkg.youtube.title, 70) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Description Field
                    ContentFieldGroup(
                        label = "DESCRIPTION",
                        content = pkg.youtube.description,
                        maxLimit = 5000,
                        onCopy = { viewModel.copyToClipboard(pkg.youtube.description, "YouTube Description") },
                        onEdit = {
                            viewModel.openEditField(platform, "description", pkg.youtube.description, 5000) { newD ->
                                viewModel.updatePlatformField(platform, "description", newD)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hashtags & Video Tags
                    ContentFieldGroup(
                        label = "HASHTAGS",
                        content = pkg.youtube.hashtags.joinToString(" "),
                        maxLimit = 200,
                        onCopy = { viewModel.copyToClipboard(pkg.youtube.hashtags.joinToString(" "), "YouTube Hashtags") }
                    )

                    if (pkg.youtube.videoTags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ContentFieldGroup(
                            label = "VIDEO TAGS (COMMA SEPARATED)",
                            content = pkg.youtube.videoTags.joinToString(", "),
                            maxLimit = 500,
                            onCopy = { viewModel.copyToClipboard(pkg.youtube.videoTags.joinToString(", "), "Video Tags") }
                        )
                    }
                }

                SocialPlatform.TIKTOK -> {
                    // Hook Text
                    ContentFieldGroup(
                        label = "0-3s VIDEO HOOK",
                        content = pkg.tiktok.hookText,
                        maxLimit = 120,
                        onCopy = { viewModel.copyToClipboard(pkg.tiktok.hookText, "TikTok Hook") },
                        onEdit = {
                            viewModel.openEditField(platform, "hook", pkg.tiktok.hookText, 120) { newH ->
                                viewModel.updatePlatformField(platform, "hook", newH)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Caption
                    ContentFieldGroup(
                        label = "CAPTION",
                        content = pkg.tiktok.caption,
                        maxLimit = 2200,
                        onCopy = { viewModel.copyToClipboard(pkg.tiktok.caption, "TikTok Caption") },
                        onEdit = {
                            viewModel.openEditField(platform, "caption", pkg.tiktok.caption, 2200) { newC ->
                                viewModel.updatePlatformField(platform, "caption", newC)
                            }
                        },
                        onFixToFit = { viewModel.fixTextToFit(platform, "caption", pkg.tiktok.caption, 300) }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hashtags
                    ContentFieldGroup(
                        label = "HASHTAGS",
                        content = pkg.tiktok.hashtags.joinToString(" "),
                        maxLimit = 300,
                        onCopy = { viewModel.copyToClipboard(pkg.tiktok.hashtags.joinToString(" "), "TikTok Hashtags") }
                    )
                }

                SocialPlatform.INSTAGRAM -> {
                    // First Line Hook
                    ContentFieldGroup(
                        label = "FIRST LINE HOOK (BEFORE '...MORE')",
                        content = pkg.instagram.firstLineHook,
                        maxLimit = 125,
                        onCopy = { viewModel.copyToClipboard(pkg.instagram.firstLineHook, "Instagram Hook") },
                        onEdit = {
                            viewModel.openEditField(platform, "hook", pkg.instagram.firstLineHook, 125) { newH ->
                                viewModel.updatePlatformField(platform, "hook", newH)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Main Caption
                    ContentFieldGroup(
                        label = "REEL / FEED CAPTION",
                        content = pkg.instagram.caption,
                        maxLimit = 2200,
                        onCopy = { viewModel.copyToClipboard(pkg.instagram.caption, "Instagram Caption") },
                        onEdit = {
                            viewModel.openEditField(platform, "caption", pkg.instagram.caption, 2200) { newC ->
                                viewModel.updatePlatformField(platform, "caption", newC)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hashtags
                    ContentFieldGroup(
                        label = "HASHTAGS",
                        content = pkg.instagram.hashtags.joinToString(" "),
                        maxLimit = 300,
                        onCopy = { viewModel.copyToClipboard(pkg.instagram.hashtags.joinToString(" "), "Instagram Hashtags") }
                    )
                }

                SocialPlatform.THREADS -> {
                    // Threads Primary Post
                    ContentFieldGroup(
                        label = "PRIMARY THREADS POST",
                        content = pkg.threads.primaryPost,
                        maxLimit = 500,
                        onCopy = { viewModel.copyToClipboard(pkg.threads.primaryPost, "Threads Post") },
                        onEdit = {
                            viewModel.openEditField(platform, "post", pkg.threads.primaryPost, 500) { newP ->
                                viewModel.updatePlatformField(platform, "post", newP)
                            }
                        },
                        onFixToFit = { viewModel.fixTextToFit(platform, "post", pkg.threads.primaryPost, 480) }
                    )

                    if (pkg.threads.threadContinuation.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ContentFieldGroup(
                            label = "THREAD REPLY CONTINUATION",
                            content = pkg.threads.threadContinuation.joinToString("\n\n"),
                            maxLimit = 1000,
                            onCopy = { viewModel.copyToClipboard(pkg.threads.threadContinuation.joinToString("\n\n"), "Thread Replies") }
                        )
                    }
                }

                SocialPlatform.LINKEDIN -> {
                    // LinkedIn Main Post
                    ContentFieldGroup(
                        label = "MAIN POST (SHORT 1-2 LINE PARAGRAPHS)",
                        content = pkg.linkedin.mainPost,
                        maxLimit = 3000,
                        onCopy = { viewModel.copyToClipboard(pkg.linkedin.mainPost, "LinkedIn Post") },
                        onEdit = {
                            viewModel.openEditField(platform, "post", pkg.linkedin.mainPost, 3000) { newP ->
                                viewModel.updatePlatformField(platform, "post", newP)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hashtags
                    ContentFieldGroup(
                        label = "PROFESSIONAL HASHTAGS",
                        content = pkg.linkedin.hashtags.joinToString(" "),
                        maxLimit = 200,
                        onCopy = { viewModel.copyToClipboard(pkg.linkedin.hashtags.joinToString(" "), "LinkedIn Hashtags") }
                    )
                }

                SocialPlatform.FACEBOOK -> {
                    // Facebook Main Post
                    ContentFieldGroup(
                        label = "COMMUNITY POST",
                        content = pkg.facebook.mainPost,
                        maxLimit = 63206,
                        onCopy = { viewModel.copyToClipboard(pkg.facebook.mainPost, "Facebook Post") },
                        onEdit = {
                            viewModel.openEditField(platform, "post", pkg.facebook.mainPost, 63206) { newP ->
                                viewModel.updatePlatformField(platform, "post", newP)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Hashtags
                    ContentFieldGroup(
                        label = "HASHTAGS",
                        content = pkg.facebook.hashtags.joinToString(" "),
                        maxLimit = 200,
                        onCopy = { viewModel.copyToClipboard(pkg.facebook.hashtags.joinToString(" "), "Facebook Hashtags") }
                    )
                }

                SocialPlatform.X -> {
                    // X Standard Post
                    ContentFieldGroup(
                        label = "STANDARD POST (MAX 280 CHARS)",
                        content = pkg.x.standardPost,
                        maxLimit = 280,
                        onCopy = { viewModel.copyToClipboard(pkg.x.standardPost, "X Post") },
                        onEdit = {
                            viewModel.openEditField(platform, "post", pkg.x.standardPost, 280) { newP ->
                                viewModel.updatePlatformField(platform, "post", newP)
                            }
                        },
                        onFixToFit = { viewModel.fixTextToFit(platform, "post", pkg.x.standardPost, 275) }
                    )

                    if (pkg.x.thread.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(12.dp))
                        ContentFieldGroup(
                            label = "THREAD BREAKDOWN",
                            content = pkg.x.thread.joinToString("\n\n"),
                            maxLimit = 1200,
                            onCopy = { viewModel.copyToClipboard(pkg.x.thread.joinToString("\n\n"), "X Thread") }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Row: Copy All, Regenerate, Share
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        val fullText = getPlatformFullString(platform, pkg)
                        viewModel.copyToClipboard(fullText, "${platform.displayName} Full Post")
                    },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Filled.ContentCopy, contentDescription = "Copy All", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy All", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { viewModel.openRegenerateSheet(platform) },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Filled.Refresh, contentDescription = "Regenerate", modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Regenerate", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ContentFieldGroup(
    label: String,
    content: String,
    maxLimit: Int,
    onCopy: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onFixToFit: (() -> Unit)? = null
) {
    Surface(
        shape = RoundedCornerShape(10.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f),
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 0.5.sp
                )

                CharacterLimitBadge(
                    currentLength = content.length,
                    maxLength = maxLimit,
                    onFixToFit = onFixToFit
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (onEdit != null) {
                    TextButton(
                        onClick = onEdit,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(imageVector = Icons.Outlined.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Edit", style = MaterialTheme.typography.labelSmall)
                    }
                }

                Spacer(modifier = Modifier.width(4.dp))

                TextButton(
                    onClick = onCopy,
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(imageVector = Icons.Outlined.ContentCopy, contentDescription = "Copy", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}

fun getPlatformFullString(platform: SocialPlatform, pkg: PlatformPackage): String {
    return when (platform) {
        SocialPlatform.YOUTUBE -> """
        ${pkg.youtube.title}
        
        ${pkg.youtube.description}
        
        Tags: ${pkg.youtube.videoTags.joinToString(", ")}
        """.trimIndent()

        SocialPlatform.TIKTOK -> """
        ${pkg.tiktok.hookText}
        
        ${pkg.tiktok.caption}
        
        ${pkg.tiktok.hashtags.joinToString(" ")}
        """.trimIndent()

        SocialPlatform.INSTAGRAM -> """
        ${pkg.instagram.firstLineHook}
        
        ${pkg.instagram.caption}
        
        ${pkg.instagram.hashtags.joinToString(" ")}
        """.trimIndent()

        SocialPlatform.THREADS -> """
        ${pkg.threads.primaryPost}
        
        ${if (pkg.threads.threadContinuation.isNotEmpty()) "\n" + pkg.threads.threadContinuation.joinToString("\n") else ""}
        """.trimIndent()

        SocialPlatform.LINKEDIN -> """
        ${pkg.linkedin.mainPost}
        
        ${pkg.linkedin.hashtags.joinToString(" ")}
        """.trimIndent()

        SocialPlatform.FACEBOOK -> """
        ${pkg.facebook.mainPost}
        
        ${pkg.facebook.hashtags.joinToString(" ")}
        """.trimIndent()

        SocialPlatform.X -> """
        ${pkg.x.standardPost}
        
        ${if (pkg.x.thread.isNotEmpty()) "\nThread:\n" + pkg.x.thread.joinToString("\n\n") else ""}
        """.trimIndent()
    }
}

fun buildFullPackageExport(socialPkg: SocialPackage, pkg: PlatformPackage): String {
    val sb = StringBuilder()
    sb.append("MANULA SOCIAL AI - FULL PUBLISHING PACKAGE\n")
    sb.append("Original Idea: ${socialPkg.originalIdea}\n")
    sb.append("Language: ${socialPkg.language.displayName} | Tone: ${socialPkg.tone.displayName}\n")
    sb.append("=========================================\n\n")

    socialPkg.selectedPlatforms.forEach { platform ->
        sb.append("[${platform.displayName.uppercase()}]\n")
        sb.append(getPlatformFullString(platform, pkg))
        sb.append("\n\n-----------------------------------------\n\n")
    }

    return sb.toString().trim()
}

data class Tuple4<A, B, C, D>(val a: A, val b: B, val c: C, val d: D)
