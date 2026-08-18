package com.example.domain.model

enum class SocialPlatform(
    val displayName: String,
    val brandColorHex: Long,
    val iconName: String
) {
    YOUTUBE("YouTube", 0xFFFF0000, "youtube"),
    TIKTOK("TikTok", 0xFF00F2FE, "tiktok"),
    INSTAGRAM("Instagram", 0xFFE1306C, "instagram"),
    THREADS("Threads", 0xFF101010, "threads"),
    LINKEDIN("LinkedIn", 0xFF0A66C2, "linkedin"),
    FACEBOOK("Facebook", 0xFF1877F2, "facebook"),
    X("X", 0xFF000000, "x")
}

enum class ContentType(val displayName: String, val description: String) {
    SHORT_VIDEO("Short video", "Short-form vertical video (Shorts, Reels, TikTok)"),
    LONG_VIDEO("Long video", "Detailed horizontal video content"),
    IMAGE_POST("Image post", "Visual single image post with engaging caption"),
    CAROUSEL("Carousel", "Multi-slide swipeable card / carousel content"),
    TEXT_POST("Text post", "Thought leadership or status post"),
    ANNOUNCEMENT("Announcement", "News, launch, or important update"),
    EDUCATIONAL("Educational", "Step-by-step knowledge sharing"),
    PROMOTIONAL("Promotional", "Offer, campaign, or lead generation"),
    NEWS("News", "Current events and topical breakdown"),
    PERSONAL("Personal", "First-person story and real life experience"),
    PRODUCT("Product", "Features, benefits, and launch showcase"),
    TUTORIAL("Tutorial", "Practical how-to guide"),
    STORYTELLING("Storytelling", "Narrative with hook, tension, and resolution")
}

enum class LanguageOption(val displayName: String, val code: String) {
    AUTO("Auto Detect", "auto"),
    SINHALA("Sinhala (සිංහල)", "si"),
    ENGLISH("English", "en"),
    MIXED("Sinhala + English (Mixed)", "si_en")
}

enum class ContentGoal(val displayName: String) {
    REACH("Maximum Reach"),
    ENGAGEMENT("Audience Engagement"),
    EDUCATION("Educational Value"),
    TRAFFIC("Website / Link Traffic"),
    LEADS("Lead Generation"),
    SALES("Product Sales"),
    PERSONAL_BRANDING("Personal Branding"),
    COMMUNITY_BUILDING("Community Building")
}

enum class ContentTone(val displayName: String) {
    PROFESSIONAL("Professional"),
    FRIENDLY("Friendly"),
    CASUAL("Casual"),
    EDUCATIONAL("Educational"),
    BOLD("Bold & Direct"),
    TECHNICAL("Technical"),
    STORYTELLING("Storytelling"),
    FUNNY("Humorous / Witty"),
    SERIOUS("Authoritative & Serious")
}

enum class RegenStyle(val label: String, val instruction: String) {
    BALANCED("Balanced", "Provide a balanced, high-retention variation."),
    MORE_VIRAL("More Viral Hook", "Focus on a powerful, high-curiosity psychological hook and punchy pacing."),
    MORE_PROFESSIONAL("More Professional", "Adopt a sophisticated, credible, industry-expert tone with clear structure."),
    SHORTER("Shorter & Punchier", "Make the content concise, crisp, and direct to the point with zero fluff."),
    MORE_CASUAL("More Casual & Friendly", "Use relatable, conversational, natural everyday creator tone."),
    MORE_SEO_FOCUSED("More SEO-Focused", "Optimize title and description for search intent, relevant keywords, and discovery."),
    MORE_EDUCATIONAL("More Educational", "Focus on actionable steps, clear takeaways, and structured value."),
    MORE_EMOTIONAL("More Emotional & Story-Driven", "Emphasize vulnerability, personal stakes, and narrative impact.")
}

enum class VariationType(val label: String, val subtitle: String) {
    A("Version A", "Balanced & High Retention"),
    B("Version B", "Curiosity & Strong Hook"),
    C("Version C", "Direct Value & Educational")
}
