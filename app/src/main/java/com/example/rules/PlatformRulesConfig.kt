package com.example.rules

import com.example.domain.model.SocialPlatform

data class FieldLimitRule(
    val fieldName: String,
    val maxChars: Int,
    val recommendedMinChars: Int,
    val recommendedMaxChars: Int,
    val isHardLimit: Boolean,
    val guidance: String
)

data class PlatformRule(
    val platform: SocialPlatform,
    val fields: Map<String, FieldLimitRule>,
    val maxHashtags: Int,
    val recommendedHashtagsRange: IntRange,
    val coreToneSummary: String,
    val formattingRules: List<String>
)

object PlatformRulesConfig {
    val rules: Map<SocialPlatform, PlatformRule> = mapOf(
        SocialPlatform.YOUTUBE to PlatformRule(
            platform = SocialPlatform.YOUTUBE,
            fields = mapOf(
                "title" to FieldLimitRule(
                    fieldName = "Title",
                    maxChars = 100,
                    recommendedMinChars = 45,
                    recommendedMaxChars = 70,
                    isHardLimit = true,
                    guidance = "Primary topic first. First 60-70 characters are crucial for mobile and search display."
                ),
                "description" to FieldLimitRule(
                    fieldName = "Description",
                    maxChars = 5000,
                    recommendedMinChars = 400,
                    recommendedMaxChars = 2000,
                    isHardLimit = true,
                    guidance = "Structure: Hook (first 3 lines), Value summary, Timestamps/Chapters, CTA, Links, Hashtags."
                ),
                "tags" to FieldLimitRule(
                    fieldName = "Tags",
                    maxChars = 500,
                    recommendedMinChars = 100,
                    recommendedMaxChars = 400,
                    isHardLimit = true,
                    guidance = "Relevant search terms comma-separated. Do not repeat keywords artificially."
                )
            ),
            maxHashtags = 15,
            recommendedHashtagsRange = 3..5,
            coreToneSummary = "Searchable + Informative + High-Value",
            formattingRules = listOf(
                "First 3 lines of description appear before 'Show more' fold.",
                "Include clean timestamp chapters for videos over 2 minutes.",
                "Avoid misleading clickbait titles that hurt audience retention."
            )
        ),
        SocialPlatform.TIKTOK to PlatformRule(
            platform = SocialPlatform.TIKTOK,
            fields = mapOf(
                "caption" to FieldLimitRule(
                    fieldName = "Caption",
                    maxChars = 2200,
                    recommendedMinChars = 80,
                    recommendedMaxChars = 300,
                    isHardLimit = true,
                    guidance = "Fast, conversational hook. Native creator pacing without YouTube-style blocks."
                ),
                "hook" to FieldLimitRule(
                    fieldName = "Hook Text",
                    maxChars = 120,
                    recommendedMinChars = 20,
                    recommendedMaxChars = 60,
                    isHardLimit = false,
                    guidance = "Must stop scrolling in 0-3 seconds. Bold curiosity or immediate value gap."
                )
            ),
            maxHashtags = 10,
            recommendedHashtagsRange = 3..6,
            coreToneSummary = "Fast + Conversational + Hook-Focused",
            formattingRules = listOf(
                "Use high-retention text hooks overlaid in the first 3 seconds.",
                "End with an interactive question or comment bait CTA.",
                "Use 3-5 niche-specific and search-optimized keywords."
            )
        ),
        SocialPlatform.INSTAGRAM to PlatformRule(
            platform = SocialPlatform.INSTAGRAM,
            fields = mapOf(
                "caption" to FieldLimitRule(
                    fieldName = "Caption",
                    maxChars = 2200,
                    recommendedMinChars = 120,
                    recommendedMaxChars = 600,
                    isHardLimit = true,
                    guidance = "First line determines 'more' click. Use strategic line breaks for readability."
                ),
                "hook" to FieldLimitRule(
                    fieldName = "First Line Hook",
                    maxChars = 125,
                    recommendedMinChars = 30,
                    recommendedMaxChars = 90,
                    isHardLimit = false,
                    guidance = "Appears before the '...more' truncation fold. Make it irresistible."
                )
            ),
            maxHashtags = 30,
            recommendedHashtagsRange = 5..8,
            coreToneSummary = "Readable + Visual + Highly Engaging",
            formattingRules = listOf(
                "Use clean bullet points and spacious paragraph line breaks.",
                "Integrate clear Save and Share call-to-actions.",
                "Mix 2 broad, 3 niche, and 2 specific hashtags."
            )
        ),
        SocialPlatform.THREADS to PlatformRule(
            platform = SocialPlatform.THREADS,
            fields = mapOf(
                "post" to FieldLimitRule(
                    fieldName = "Primary Post",
                    maxChars = 500,
                    recommendedMinChars = 100,
                    recommendedMaxChars = 350,
                    isHardLimit = true,
                    guidance = "Conversational, direct, discussion-sparking. Not a mini-LinkedIn essay."
                )
            ),
            maxHashtags = 5,
            recommendedHashtagsRange = 1..2,
            coreToneSummary = "Conversational + Unfiltered + Discussion-Oriented",
            formattingRules = listOf(
                "Write like speaking to peers in a casual group chat.",
                "Pose an open debate or opinion prompt at the end.",
                "Keep hashtags minimal (1 topic tag is often best)."
            )
        ),
        SocialPlatform.LINKEDIN to PlatformRule(
            platform = SocialPlatform.LINKEDIN,
            fields = mapOf(
                "post" to FieldLimitRule(
                    fieldName = "Main Post",
                    maxChars = 3000,
                    recommendedMinChars = 500,
                    recommendedMaxChars = 1400,
                    isHardLimit = true,
                    guidance = "Strong 1-2 line opening hook. Short paragraphs (1-2 sentences), actionable lessons."
                ),
                "hook" to FieldLimitRule(
                    fieldName = "Hook (First 2 Lines)",
                    maxChars = 140,
                    recommendedMinChars = 40,
                    recommendedMaxChars = 100,
                    isHardLimit = false,
                    guidance = "Crucial before 'see more' button. State the problem, outcome, or counter-intuitive insight."
                )
            ),
            maxHashtags = 8,
            recommendedHashtagsRange = 3..5,
            coreToneSummary = "Professional + Insightful + Experience-Driven",
            formattingRules = listOf(
                "Use 1-2 line whitespace spacing between thoughts for effortless scanning.",
                "Avoid corporate jargon and empty motivational clichés.",
                "State real lessons, specific numbers, and personal takeaway takeaways."
            )
        ),
        SocialPlatform.FACEBOOK to PlatformRule(
            platform = SocialPlatform.FACEBOOK,
            fields = mapOf(
                "post" to FieldLimitRule(
                    fieldName = "Main Post",
                    maxChars = 63206,
                    recommendedMinChars = 200,
                    recommendedMaxChars = 800,
                    isHardLimit = true,
                    guidance = "Community-friendly, warm, relatable. Great for stories, questions, and practical tips."
                )
            ),
            maxHashtags = 10,
            recommendedHashtagsRange = 2..4,
            coreToneSummary = "Community-Friendly + Relatable + Story-Driven",
            formattingRules = listOf(
                "Encourage comments with relatable everyday situations.",
                "Use conversational language with authentic personal touch.",
                "Provide a short snappy alternative for quick readers."
            )
        ),
        SocialPlatform.X to PlatformRule(
            platform = SocialPlatform.X,
            fields = mapOf(
                "post" to FieldLimitRule(
                    fieldName = "Standard Post",
                    maxChars = 280,
                    recommendedMinChars = 120,
                    recommendedMaxChars = 270,
                    isHardLimit = true,
                    guidance = "Standard 280 character limit strictly observed. High punch-per-word density."
                )
            ),
            maxHashtags = 4,
            recommendedHashtagsRange = 1..2,
            coreToneSummary = "Concise + Sharp + Discussion-Friendly",
            formattingRules = listOf(
                "Every word must fight for its spot.",
                "First line delivers immediate perspective or tension.",
                "Thread starts with strongest hook, finishes with CTA."
            )
        )
    )

    fun getRule(platform: SocialPlatform): PlatformRule =
        rules[platform] ?: rules.getValue(SocialPlatform.X)

    fun getFieldLimit(platform: SocialPlatform, fieldKey: String): FieldLimitRule? =
        getRule(platform).fields[fieldKey]
}
