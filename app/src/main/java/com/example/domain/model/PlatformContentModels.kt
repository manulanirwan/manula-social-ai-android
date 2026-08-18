package com.example.domain.model

data class YouTubeContent(
    val title: String = "",
    val description: String = "",
    val hashtags: List<String> = emptyList(),
    val videoTags: List<String> = emptyList(),
    val chaptersSuggestion: List<String> = emptyList(),
    val cta: String = "",
    val pinnedComment: String = "",
    val hookVariants: List<String> = emptyList(),
    val searchKeywords: List<String> = emptyList()
)

data class TikTokContent(
    val caption: String = "",
    val hookText: String = "",
    val hashtags: List<String> = emptyList(),
    val cta: String = "",
    val searchKeywordPhrases: List<String> = emptyList(),
    val onScreenText: List<String> = emptyList(),
    val commentPrompt: String = ""
)

data class InstagramContent(
    val caption: String = "",
    val firstLineHook: String = "",
    val cta: String = "",
    val hashtags: List<String> = emptyList(),
    val coverText: String = "",
    val pinnedComment: String = "",
    val slideSuggestions: List<String> = emptyList()
)

data class ThreadsContent(
    val primaryPost: String = "",
    val threadContinuation: List<String> = emptyList(),
    val discussionPrompt: String = "",
    val hashtags: List<String> = emptyList()
)

data class LinkedInContent(
    val hook: String = "",
    val mainPost: String = "",
    val cta: String = "",
    val hashtags: List<String> = emptyList(),
    val firstCommentStrategy: String = "",
    val postTitle: String = ""
)

data class FacebookContent(
    val mainPost: String = "",
    val shortVersion: String = "",
    val cta: String = "",
    val hashtags: List<String> = emptyList(),
    val firstComment: String = "",
    val communityQuestion: String = ""
)

data class XContent(
    val standardPost: String = "",
    val alternativeVersion: String = "",
    val thread: List<String> = emptyList(),
    val cta: String = "",
    val hashtags: List<String> = emptyList()
)

data class PlatformPackage(
    val youtube: YouTubeContent = YouTubeContent(),
    val tiktok: TikTokContent = TikTokContent(),
    val instagram: InstagramContent = InstagramContent(),
    val threads: ThreadsContent = ThreadsContent(),
    val linkedin: LinkedInContent = LinkedInContent(),
    val facebook: FacebookContent = FacebookContent(),
    val x: XContent = XContent()
)

data class AISuggestion(
    val platform: SocialPlatform,
    val field: String,
    val type: SuggestionType,
    val message: String,
    val recommendedFix: String? = null
)

enum class SuggestionType {
    LIMIT_EXCEEDED,
    LIMIT_WARNING,
    HOOK_IMPROVEMENT,
    SEO_DISCOVERY,
    TONE_ALIGNMENT,
    HASHTAG_OPTIMIZATION,
    READABILITY
}

data class SocialPackage(
    val originalIdea: String,
    val contentType: ContentType,
    val language: LanguageOption,
    val tone: ContentTone,
    val goal: ContentGoal,
    val targetAudience: String,
    val brandName: String,
    val creatorName: String,
    val productName: String,
    val websiteUrl: String,
    val mainKeyword: String,
    val location: String,
    val selectedPlatforms: Set<SocialPlatform>,
    val versionA: PlatformPackage,
    val versionB: PlatformPackage,
    val versionC: PlatformPackage,
    val aiSuggestions: List<AISuggestion> = emptyList(),
    val qualityScore: Int = 96,
    val createdAt: Long = System.currentTimeMillis()
)

data class BrandProfile(
    val id: Long = 0,
    val name: String,
    val description: String = "",
    val website: String = "",
    val mainTopics: String = "",
    val preferredLanguage: LanguageOption = LanguageOption.AUTO,
    val preferredTone: ContentTone = ContentTone.PROFESSIONAL,
    val targetAudience: String = "",
    val defaultCta: String = "",
    val brandHashtags: List<String> = emptyList(),
    val emojiUsage: String = "Moderate & relevant",
    val isActive: Boolean = false
)
