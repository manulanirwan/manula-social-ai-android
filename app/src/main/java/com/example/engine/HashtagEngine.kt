package com.example.engine

import com.example.domain.model.SocialPlatform

data class CategorizedHashtags(
    val broad: List<String>,
    val niche: List<String>,
    val specific: List<String>,
    val brand: List<String>
) {
    fun allHashtags(): List<String> {
        val list = mutableListOf<String>()
        list.addAll(brand)
        list.addAll(specific)
        list.addAll(niche)
        list.addAll(broad)
        return list.distinct()
    }
}

object HashtagEngine {

    fun generatePlatformHashtags(
        topic: String,
        platform: SocialPlatform,
        brandName: String = "",
        existingHashtags: List<String> = emptyList(),
        extraKeywords: List<String> = emptyList()
    ): CategorizedHashtags {
        val cleanTopic = topic.lowercase().replace(Regex("[^a-zA-Z0-9\\s\\u0D80-\\u0DFF]"), "")
        val words = cleanTopic.split(Regex("\\s+")).filter { it.length > 2 }

        val broadList = mutableListOf<String>()
        val nicheList = mutableListOf<String>()
        val specificList = mutableListOf<String>()
        val brandList = mutableListOf<String>()

        if (brandName.isNotBlank()) {
            val formattedBrand = "#" + brandName.replace(" ", "").replace("#", "")
            brandList.add(formattedBrand)
        }

        // Platform specific base broad tags
        when (platform) {
            SocialPlatform.YOUTUBE -> {
                broadList.addAll(listOf("#Shorts", "#YouTubeTips", "#Creator", "#ViralVideo"))
            }
            SocialPlatform.TIKTOK -> {
                broadList.addAll(listOf("#fyp", "#viral", "#trending", "#foryoupage", "#learnontiktok"))
            }
            SocialPlatform.INSTAGRAM -> {
                broadList.addAll(listOf("#reelsinstagram", "#creators", "#explorepage", "#instadaily"))
            }
            SocialPlatform.THREADS -> {
                broadList.addAll(listOf("#threads", "#dailythoughts"))
            }
            SocialPlatform.LINKEDIN -> {
                broadList.addAll(listOf("#management", "#innovation", "#leadership", "#careers"))
            }
            SocialPlatform.FACEBOOK -> {
                broadList.addAll(listOf("#trending", "#community", "#tipsandtricks"))
            }
            SocialPlatform.X -> {
                broadList.addAll(listOf("#TechTrends", "#CreatorEconomy"))
            }
        }

        // Detect topic domains
        val lowerTopic = topic.lowercase()
        val isSinhala = topic.any { it in '\u0D80'..'\u0DFF' }
        val isTech = listOf("ai", "code", "website", "software", "app", "tech", "tools", "automation", "python", "javascript", "developer", "computer").any { lowerTopic.contains(it) }
        val isBusiness = listOf("business", "money", "sales", "marketing", "startup", "ecommerce", "income", "freelance").any { lowerTopic.contains(it) }
        val isEducation = listOf("learn", "study", "tutorial", "guide", "tips", "how to", "class", "knowledge").any { lowerTopic.contains(it) }

        if (isSinhala) {
            nicheList.addAll(listOf("#SinhalaContent", "#SriLankaCreators", "#SinhalaTech", "#SLTech"))
            if (isTech) specificList.addAll(listOf("#AItoolsSinhala", "#WebDevelopmentSL", "#SinhalaIT"))
            if (isBusiness) specificList.addAll(listOf("#BusinessSinhala", "#FreelanceSriLanka", "#IncomeSL"))
        }

        if (isTech) {
            nicheList.addAll(listOf("#ArtificialIntelligence", "#WebDevelopment", "#TechInnovation", "#FutureOfTech"))
            specificList.addAll(listOf("#AITools", "#WebDesignTips", "#NoCode", "#DevLife", "#PromptEngineering"))
        } else if (isBusiness) {
            nicheList.addAll(listOf("#Entrepreneurship", "#DigitalMarketing", "#GrowthMindset", "#BusinessStrategy"))
            specificList.addAll(listOf("#ContentCreatorStrategy", "#OnlineBusiness", "#LeadGeneration"))
        } else if (isEducation) {
            nicheList.addAll(listOf("#EducationFirst", "#SkillUp", "#ProductivityHacks"))
            specificList.addAll(listOf("#HowToGuide", "#QuickTutorial", "#LearnSomethingNew"))
        } else {
            nicheList.addAll(listOf("#ContentCreation", "#DigitalStrategy", "#CreatorLife"))
            specificList.addAll(listOf("#CreativeProcess", "#SocialGrowth", "#Storytelling"))
        }

        // Add topic-derived tags
        words.take(4).forEach { word ->
            if (word.length > 3 && !listOf("video", "post", "make", "about", "this", "what", "with", "ekak", "karanawa", "gana").contains(word)) {
                val tag = "#" + word.replaceFirstChar { it.uppercase() }
                if (!specificList.contains(tag) && !broadList.contains(tag)) {
                    specificList.add(tag)
                }
            }
        }

        // Add extra keywords
        extraKeywords.forEach { kw ->
            val cleanKw = kw.replace(" ", "").replace("#", "")
            if (cleanKw.isNotBlank()) {
                specificList.add("#$cleanKw")
            }
        }

        // Incorporate existing tags if provided
        existingHashtags.forEach { existing ->
            val formatted = if (existing.startsWith("#")) existing else "#$existing"
            if (!specificList.contains(formatted) && !nicheList.contains(formatted) && !broadList.contains(formatted)) {
                specificList.add(formatted)
            }
        }

        return CategorizedHashtags(
            broad = broadList.distinct().take(4),
            niche = nicheList.distinct().take(4),
            specific = specificList.distinct().take(6),
            brand = brandList.distinct()
        )
    }

    fun cleanHashtagString(hashtags: List<String>): String =
        hashtags.joinToString(" ") { if (it.startsWith("#")) it else "#$it" }
}
