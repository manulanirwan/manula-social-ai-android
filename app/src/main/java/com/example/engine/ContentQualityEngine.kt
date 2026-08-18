package com.example.engine

import com.example.domain.model.AISuggestion
import com.example.domain.model.PlatformPackage
import com.example.domain.model.SocialPlatform
import com.example.domain.model.SuggestionType
import com.example.rules.PlatformRulesConfig

object ContentQualityEngine {

    data class ValidationResult(
        val qualityScore: Int,
        val suggestions: List<AISuggestion>,
        val hasHardLimitViolations: Boolean
    )

    fun evaluatePackage(pkg: PlatformPackage, selectedPlatforms: Set<SocialPlatform>): ValidationResult {
        val suggestions = mutableListOf<AISuggestion>()
        var deductions = 0
        var hardViolation = false

        if (selectedPlatforms.contains(SocialPlatform.YOUTUBE)) {
            val ytRule = PlatformRulesConfig.getRule(SocialPlatform.YOUTUBE)
            val titleLimit = ytRule.fields["title"]?.maxChars ?: 100
            val titleLen = pkg.youtube.title.length
            if (titleLen > titleLimit) {
                hardViolation = true
                deductions += 15
                suggestions.add(
                    AISuggestion(
                        platform = SocialPlatform.YOUTUBE,
                        field = "Title",
                        type = SuggestionType.LIMIT_EXCEEDED,
                        message = "YouTube Title exceeds limit: $titleLen / $titleLimit chars. Mobile feeds will cut this off.",
                        recommendedFix = fixToFit(pkg.youtube.title, 70)
                    )
                )
            } else if (titleLen > 75) {
                deductions += 3
                suggestions.add(
                    AISuggestion(
                        platform = SocialPlatform.YOUTUBE,
                        field = "Title",
                        type = SuggestionType.LIMIT_WARNING,
                        message = "YouTube Title is $titleLen chars. Keeping it under 70 chars boosts mobile click-through rate."
                    )
                )
            }
            if (pkg.youtube.hashtags.size > 15) {
                deductions += 5
                suggestions.add(
                    AISuggestion(
                        platform = SocialPlatform.YOUTUBE,
                        field = "Hashtags",
                        type = SuggestionType.HASHTAG_OPTIMIZATION,
                        message = "YouTube ignores all hashtags if more than 15 are added. Recommended: 3-5 tags."
                    )
                )
            }
        }

        if (selectedPlatforms.contains(SocialPlatform.X)) {
            val xRule = PlatformRulesConfig.getRule(SocialPlatform.X)
            val maxChars = xRule.fields["post"]?.maxChars ?: 280
            val postLen = pkg.x.standardPost.length
            if (postLen > maxChars) {
                hardViolation = true
                deductions += 20
                suggestions.add(
                    AISuggestion(
                        platform = SocialPlatform.X,
                        field = "Standard Post",
                        type = SuggestionType.LIMIT_EXCEEDED,
                        message = "X post exceeds standard limit: $postLen / $maxChars characters.",
                        recommendedFix = fixToFit(pkg.x.standardPost, 275)
                    )
                )
            }
        }

        if (selectedPlatforms.contains(SocialPlatform.TIKTOK)) {
            val captionLen = pkg.tiktok.caption.length
            if (captionLen > 2200) {
                hardViolation = true
                deductions += 15
                suggestions.add(
                    AISuggestion(
                        platform = SocialPlatform.TIKTOK,
                        field = "Caption",
                        type = SuggestionType.LIMIT_EXCEEDED,
                        message = "TikTok caption exceeds 2,200 character limit.",
                        recommendedFix = fixToFit(pkg.tiktok.caption, 300)
                    )
                )
            } else if (captionLen > 400) {
                deductions += 4
                suggestions.add(
                    AISuggestion(
                        platform = SocialPlatform.TIKTOK,
                        field = "Caption",
                        type = SuggestionType.READABILITY,
                        message = "TikTok audience favors short, punchy captions under 300 characters for high retention."
                    )
                )
            }
        }

        if (selectedPlatforms.contains(SocialPlatform.THREADS)) {
            val postLen = pkg.threads.primaryPost.length
            if (postLen > 500) {
                hardViolation = true
                deductions += 15
                suggestions.add(
                    AISuggestion(
                        platform = SocialPlatform.THREADS,
                        field = "Primary Post",
                        type = SuggestionType.LIMIT_EXCEEDED,
                        message = "Threads post exceeds 500 character limit ($postLen chars).",
                        recommendedFix = fixToFit(pkg.threads.primaryPost, 480)
                    )
                )
            }
        }

        if (selectedPlatforms.contains(SocialPlatform.LINKEDIN)) {
            val postLen = pkg.linkedin.mainPost.length
            if (postLen > 3000) {
                hardViolation = true
                deductions += 15
                suggestions.add(
                    AISuggestion(
                        platform = SocialPlatform.LINKEDIN,
                        field = "Main Post",
                        type = SuggestionType.LIMIT_EXCEEDED,
                        message = "LinkedIn post exceeds 3,000 character limit ($postLen chars).",
                        recommendedFix = fixToFit(pkg.linkedin.mainPost, 2000)
                    )
                )
            }
            if (!pkg.linkedin.mainPost.contains("\n\n") && postLen > 200) {
                deductions += 5
                suggestions.add(
                    AISuggestion(
                        platform = SocialPlatform.LINKEDIN,
                        field = "Main Post",
                        type = SuggestionType.READABILITY,
                        message = "LinkedIn posts need short 1-2 sentence paragraphs with whitespace for mobile readability."
                    )
                )
            }
        }

        if (selectedPlatforms.contains(SocialPlatform.INSTAGRAM)) {
            val captionLen = pkg.instagram.caption.length
            if (captionLen > 2200) {
                hardViolation = true
                deductions += 15
                suggestions.add(
                    AISuggestion(
                        platform = SocialPlatform.INSTAGRAM,
                        field = "Caption",
                        type = SuggestionType.LIMIT_EXCEEDED,
                        message = "Instagram caption exceeds 2,200 character limit.",
                        recommendedFix = fixToFit(pkg.instagram.caption, 1000)
                    )
                )
            }
        }

        val score = (100 - deductions).coerceIn(40, 100)
        return ValidationResult(
            qualityScore = score,
            suggestions = suggestions,
            hasHardLimitViolations = hardViolation
        )
    }

    /**
     * Smart intelligent "Fix to Fit" trimmer that preserves hooks, sentences, and CTAs.
     */
    fun fixToFit(text: String, maxLimit: Int): String {
        if (text.length <= maxLimit) return text

        val sentences = text.split(Regex("(?<=[.!?\\n])\\s+")).filter { it.isNotBlank() }
        val builder = StringBuilder()

        for (sentence in sentences) {
            if (builder.length + sentence.length + 1 <= maxLimit - 3) {
                if (builder.isNotEmpty()) builder.append(" ")
                builder.append(sentence.trim())
            } else {
                break
            }
        }

        if (builder.isEmpty() || builder.length < maxLimit / 2) {
            // Cut at last space before maxLimit - 3
            val cutoff = text.substring(0, (maxLimit - 3).coerceAtMost(text.length))
            val lastSpace = cutoff.lastIndexOf(' ')
            return if (lastSpace > 0) {
                cutoff.substring(0, lastSpace).trim() + "..."
            } else {
                cutoff.trim() + "..."
            }
        }

        return builder.toString().trim()
    }
}
