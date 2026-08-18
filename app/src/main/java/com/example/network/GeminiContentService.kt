package com.example.network

import com.example.BuildConfig
import com.example.domain.model.*
import com.example.engine.HashtagEngine
import org.json.JSONArray
import org.json.JSONObject

class GeminiContentService(
    private val apiService: GeminiApiService = RetrofitClient.geminiService
) {
    // Current stable and fast model
    var selectedModel: String = "gemini-2.5-flash"
    var temperature: Float = 0.7f

    suspend fun generateSocialPackages(
        idea: String,
        contentType: ContentType,
        language: LanguageOption,
        tone: ContentTone,
        goal: ContentGoal,
        targetAudience: String,
        brandProfile: BrandProfile?,
        selectedPlatforms: Set<SocialPlatform>,
        variationType: VariationType = VariationType.A,
        regenInstruction: String? = null
    ): PlatformPackage {
        val apiKey = BuildConfig.GEMINI_API_KEY.takeIf { it.isNotBlank() && it != "MY_GEMINI_API_KEY" }

        if (!apiKey.isNullOrBlank()) {
            try {
                val prompt = buildPrompt(
                    idea = idea,
                    contentType = contentType,
                    language = language,
                    tone = tone,
                    goal = goal,
                    targetAudience = targetAudience,
                    brandProfile = brandProfile,
                    selectedPlatforms = selectedPlatforms,
                    variationType = variationType,
                    regenInstruction = regenInstruction
                )

                val request = GeminiRequest(
                    contents = listOf(
                        ContentItem(parts = listOf(PartItem(text = prompt)))
                    ),
                    generationConfig = GenerationConfig(
                        temperature = temperature,
                        topP = 0.95f,
                        topK = 40,
                        responseMimeType = "application/json"
                    ),
                    systemInstruction = ContentItem(
                        parts = listOf(
                            PartItem(
                                text = "You are MANULA SOCIAL AI, an elite world-class multi-platform social media creator and strategist. " +
                                        "You generate highly differentiated, platform-native publishing packages adhering strictly to official character limits, " +
                                        "structure, hooks, and audience psychology. Never duplicate the same caption across platforms. " +
                                        "Support English, Sinhala (සිංහල), and Sinhala-English mixed language fluently. Output valid JSON only."
                            )
                        )
                    )
                )

                val response = apiService.generateContent(
                    model = selectedModel,
                    apiKey = apiKey,
                    request = request
                )

                val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (!rawJson.isNullOrBlank()) {
                    return parsePlatformPackage(rawJson, idea, brandProfile, selectedPlatforms)
                }
            } catch (e: Exception) {
                // Log silently or fallback gracefully to offline generation engine
            }
        }

        // Offline / Fallback Intelligent Generator
        return generateIntelligentFallbackPackage(
            idea = idea,
            contentType = contentType,
            language = language,
            tone = tone,
            goal = goal,
            targetAudience = targetAudience,
            brandProfile = brandProfile,
            selectedPlatforms = selectedPlatforms,
            variationType = variationType,
            regenInstruction = regenInstruction
        )
    }

    private fun buildPrompt(
        idea: String,
        contentType: ContentType,
        language: LanguageOption,
        tone: ContentTone,
        goal: ContentGoal,
        targetAudience: String,
        brandProfile: BrandProfile?,
        selectedPlatforms: Set<SocialPlatform>,
        variationType: VariationType,
        regenInstruction: String?
    ): String {
        val brandContext = if (brandProfile != null && brandProfile.name.isNotBlank()) {
            """
            BRAND / CREATOR PROFILE:
            - Name: ${brandProfile.name}
            - Description: ${brandProfile.description}
            - Default CTA: ${brandProfile.defaultCta}
            - Brand Hashtags: ${brandProfile.brandHashtags.joinToString(" ")}
            - Preferred Emojis: ${brandProfile.emojiUsage}
            """.trimIndent()
        } else ""

        val platformsRequested = selectedPlatforms.joinToString(", ") { it.displayName }

        return """
        Generate a complete social media publishing package for the following request:
        
        CREATOR IDEA / TOPIC:
        "$idea"
        
        SPECIFICATIONS:
        - Content Type: ${contentType.displayName} (${contentType.description})
        - Language Mode: ${language.displayName} (Follow user language style: Sinhala, English, or Mixed Sinhala-English)
        - Target Tone: ${tone.displayName}
        - Primary Goal: ${goal.displayName}
        - Target Audience: ${if (targetAudience.isNotBlank()) targetAudience else "General Social Audience"}
        - Variation: ${variationType.label} - ${variationType.subtitle}
        ${if (!regenInstruction.isNullOrBlank()) "- Style Instruction: $regenInstruction" else ""}
        $brandContext
        
        PLATFORMS TO GENERATE: $platformsRequested
        
        RULES FOR EACH PLATFORM:
        1. YouTube (Shorts & Long):
           - Title: Max 70 chars recommended (hard limit 100). Searchable, curiosity gap, topic first.
           - Description: Structured Hook, Summary, Timestamps/Chapters, CTA, Hashtags. Max 5000 chars.
           - Hashtags: 3 to 5 high-relevance tags.
        2. TikTok:
           - Fast native hook text, short punchy caption (under 300 chars), 3-6 niche tags, comment prompt.
        3. Instagram:
           - Visual Reel/Feed caption with irresistible first-line hook before 'more', clean spacing, CTA, 5-8 tags.
        4. Threads:
           - Conversational, sharp discussion starter, concise (under 500 chars), 1-2 tags.
        5. LinkedIn:
           - Professional insight, 1-2 sentence spacing, first-person lesson, actionable takeaways, 3-5 tags.
        6. Facebook:
           - Relatable community post, short version alternative, engaging question, 2-4 tags.
        7. X (Twitter):
           - Standard post STRICTLY under 280 characters, alternative angle, optional 3-tweet thread.
           
        OUTPUT JSON SCHEMA (Strictly return ONLY this JSON object):
        {
          "youtube": {
            "title": "String",
            "description": "String",
            "hashtags": ["String"],
            "videoTags": ["String"],
            "chaptersSuggestion": ["String"],
            "cta": "String",
            "pinnedComment": "String",
            "hookVariants": ["String"],
            "searchKeywords": ["String"]
          },
          "tiktok": {
            "caption": "String",
            "hookText": "String",
            "hashtags": ["String"],
            "cta": "String",
            "searchKeywordPhrases": ["String"],
            "onScreenText": ["String"],
            "commentPrompt": "String"
          },
          "instagram": {
            "caption": "String",
            "firstLineHook": "String",
            "cta": "String",
            "hashtags": ["String"],
            "coverText": "String",
            "pinnedComment": "String",
            "slideSuggestions": ["String"]
          },
          "threads": {
            "primaryPost": "String",
            "threadContinuation": ["String"],
            "discussionPrompt": "String",
            "hashtags": ["String"]
          },
          "linkedin": {
            "hook": "String",
            "mainPost": "String",
            "cta": "String",
            "hashtags": ["String"],
            "firstCommentStrategy": "String",
            "postTitle": "String"
          },
          "facebook": {
            "mainPost": "String",
            "shortVersion": "String",
            "cta": "String",
            "hashtags": ["String"],
            "firstComment": "String",
            "communityQuestion": "String"
          },
          "x": {
            "standardPost": "String (Max 280 chars)",
            "alternativeVersion": "String",
            "thread": ["String"],
            "cta": "String",
            "hashtags": ["String"]
          }
        }
        """.trimIndent()
    }

    private fun parsePlatformPackage(
        jsonString: String,
        idea: String,
        brandProfile: BrandProfile?,
        selectedPlatforms: Set<SocialPlatform>
    ): PlatformPackage {
        val cleanJson = jsonString
            .replace("```json", "")
            .replace("```", "")
            .trim()

        val root = JSONObject(cleanJson)

        val ytObj = root.optJSONObject("youtube")
        val youtube = YouTubeContent(
            title = ytObj?.optString("title").orEmpty(),
            description = ytObj?.optString("description").orEmpty(),
            hashtags = optStringList(ytObj?.optJSONArray("hashtags")),
            videoTags = optStringList(ytObj?.optJSONArray("videoTags")),
            chaptersSuggestion = optStringList(ytObj?.optJSONArray("chaptersSuggestion")),
            cta = ytObj?.optString("cta").orEmpty(),
            pinnedComment = ytObj?.optString("pinnedComment").orEmpty(),
            hookVariants = optStringList(ytObj?.optJSONArray("hookVariants")),
            searchKeywords = optStringList(ytObj?.optJSONArray("searchKeywords"))
        )

        val ttObj = root.optJSONObject("tiktok")
        val tiktok = TikTokContent(
            caption = ttObj?.optString("caption").orEmpty(),
            hookText = ttObj?.optString("hookText").orEmpty(),
            hashtags = optStringList(ttObj?.optJSONArray("hashtags")),
            cta = ttObj?.optString("cta").orEmpty(),
            searchKeywordPhrases = optStringList(ttObj?.optJSONArray("searchKeywordPhrases")),
            onScreenText = optStringList(ttObj?.optJSONArray("onScreenText")),
            commentPrompt = ttObj?.optString("commentPrompt").orEmpty()
        )

        val igObj = root.optJSONObject("instagram")
        val instagram = InstagramContent(
            caption = igObj?.optString("caption").orEmpty(),
            firstLineHook = igObj?.optString("firstLineHook").orEmpty(),
            cta = igObj?.optString("cta").orEmpty(),
            hashtags = optStringList(igObj?.optJSONArray("hashtags")),
            coverText = igObj?.optString("coverText").orEmpty(),
            pinnedComment = igObj?.optString("pinnedComment").orEmpty(),
            slideSuggestions = optStringList(igObj?.optJSONArray("slideSuggestions"))
        )

        val thObj = root.optJSONObject("threads")
        val threads = ThreadsContent(
            primaryPost = thObj?.optString("primaryPost").orEmpty(),
            threadContinuation = optStringList(thObj?.optJSONArray("threadContinuation")),
            discussionPrompt = thObj?.optString("discussionPrompt").orEmpty(),
            hashtags = optStringList(thObj?.optJSONArray("hashtags"))
        )

        val liObj = root.optJSONObject("linkedin")
        val linkedin = LinkedInContent(
            hook = liObj?.optString("hook").orEmpty(),
            mainPost = liObj?.optString("mainPost").orEmpty(),
            cta = liObj?.optString("cta").orEmpty(),
            hashtags = optStringList(liObj?.optJSONArray("hashtags")),
            firstCommentStrategy = liObj?.optString("firstCommentStrategy").orEmpty(),
            postTitle = liObj?.optString("postTitle").orEmpty()
        )

        val fbObj = root.optJSONObject("facebook")
        val facebook = FacebookContent(
            mainPost = fbObj?.optString("mainPost").orEmpty(),
            shortVersion = fbObj?.optString("shortVersion").orEmpty(),
            cta = fbObj?.optString("cta").orEmpty(),
            hashtags = optStringList(fbObj?.optJSONArray("hashtags")),
            firstComment = fbObj?.optString("firstComment").orEmpty(),
            communityQuestion = fbObj?.optString("communityQuestion").orEmpty()
        )

        val xObj = root.optJSONObject("x")
        val x = XContent(
            standardPost = xObj?.optString("standardPost").orEmpty(),
            alternativeVersion = xObj?.optString("alternativeVersion").orEmpty(),
            thread = optStringList(xObj?.optJSONArray("thread")),
            cta = xObj?.optString("cta").orEmpty(),
            hashtags = optStringList(xObj?.optJSONArray("hashtags"))
        )

        return PlatformPackage(
            youtube = youtube,
            tiktok = tiktok,
            instagram = instagram,
            threads = threads,
            linkedin = linkedin,
            facebook = facebook,
            x = x
        )
    }

    private fun optStringList(array: JSONArray?): List<String> {
        if (array == null) return emptyList()
        val list = mutableListOf<String>()
        for (i in 0 until array.length()) {
            val str = array.optString(i)
            if (str.isNotBlank()) list.add(str)
        }
        return list
    }

    /**
     * Highly sophisticated offline template generator that responds accurately
     * in Sinhala, English, or Mixed language when offline or without API key.
     */
    fun generateIntelligentFallbackPackage(
        idea: String,
        contentType: ContentType,
        language: LanguageOption,
        tone: ContentTone,
        goal: ContentGoal,
        targetAudience: String,
        brandProfile: BrandProfile?,
        selectedPlatforms: Set<SocialPlatform>,
        variationType: VariationType,
        regenInstruction: String?
    ): PlatformPackage {
        val isSinhala = idea.any { it in '\u0D80'..'\u0DFF' } || language == LanguageOption.SINHALA
        val isMixed = language == LanguageOption.MIXED || (!isSinhala && (idea.contains("karanawa") || idea.contains("hahuwa") || idea.contains("ekak") || idea.contains("yanna")))
        val brandName = brandProfile?.name.orEmpty()
        val defaultCta = brandProfile?.defaultCta.takeIf { !it.isNullOrBlank() }

        val ytTags = HashtagEngine.generatePlatformHashtags(idea, SocialPlatform.YOUTUBE, brandName).allHashtags().take(5)
        val ttTags = HashtagEngine.generatePlatformHashtags(idea, SocialPlatform.TIKTOK, brandName).allHashtags().take(5)
        val igTags = HashtagEngine.generatePlatformHashtags(idea, SocialPlatform.INSTAGRAM, brandName).allHashtags().take(7)
        val thTags = HashtagEngine.generatePlatformHashtags(idea, SocialPlatform.THREADS, brandName).allHashtags().take(2)
        val liTags = HashtagEngine.generatePlatformHashtags(idea, SocialPlatform.LINKEDIN, brandName).allHashtags().take(4)
        val fbTags = HashtagEngine.generatePlatformHashtags(idea, SocialPlatform.FACEBOOK, brandName).allHashtags().take(4)
        val xTags = HashtagEngine.generatePlatformHashtags(idea, SocialPlatform.X, brandName).allHashtags().take(2)

        return if (isSinhala) {
            generateSinhalaPackage(idea, contentType, variationType, ytTags, ttTags, igTags, thTags, liTags, fbTags, xTags, defaultCta)
        } else if (isMixed) {
            generateMixedPackage(idea, contentType, variationType, ytTags, ttTags, igTags, thTags, liTags, fbTags, xTags, defaultCta)
        } else {
            generateEnglishPackage(idea, contentType, variationType, ytTags, ttTags, igTags, thTags, liTags, fbTags, xTags, defaultCta)
        }
    }

    private fun generateSinhalaPackage(
        idea: String,
        contentType: ContentType,
        variation: VariationType,
        ytTags: List<String>,
        ttTags: List<String>,
        igTags: List<String>,
        thTags: List<String>,
        liTags: List<String>,
        fbTags: List<String>,
        xTags: List<String>,
        ctaOverride: String?
    ): PlatformPackage {
        val topicSnippet = idea.take(60)
        val cta = ctaOverride ?: "ඔබේ අදහස් පහළින් Comment කරන්න! Channel එක Subscribe කරන්න."

        val ytTitle = when (variation) {
            VariationType.A -> "මේක දැනගෙන හිටියද? $topicSnippet සම්පූර්ණ විස්තරය"
            VariationType.B -> "කිසිවෙකු නොකියන රහස: $topicSnippet පියවරෙන් පියවර"
            VariationType.C -> "$topicSnippet ගැන සරලව ඉගෙන ගනිමු (Step-by-Step Guide)"
        }

        val ytDesc = """
        📌 මෙම වීඩියෝවෙන්: $idea
        
        අද අපි කතා කරන්නේ $topicSnippet ගැන සම්පූර්ණ හා ප්‍රායෝගික ක්‍රමවේදයයි. ඔබත් මේ ක්ෂේත්‍රයේ ඉදිරියට යන්න කැමති නම් සම්පූර්ණයෙන්ම නරඹන්න.
        
        ⏱️ Chapters / කාල රාමු:
        00:00 - හැඳින්වීම සහ ප්‍රධාන සංකල්පය
        01:15 - පියවරෙන් පියවර ක්‍රියාත්මක කිරීම
        03:40 - ප්‍රධාන වාසි සහ වැදගත් උපදෙස්
        05:20 - අවසාන නිගමනය සහ CTA
        
        🔔 $cta
        
        ${ytTags.joinToString(" ")}
        """.trimIndent()

        val ttCaption = "මේ ක්‍රමය ඔයාලත් අත්හදා බලන්න! 🚀 $topicSnippet ගැන දැනගත යුතු දේ. ඔබ හිතන්නේ මොකක්ද? පහළින් කියන්න 👇 ${ttTags.joinToString(" ")}"

        val igCaption = """
        ✨ $topicSnippet ගැන විශේෂ කරුණු කිහිපයක්!
        
        1️⃣ නිවැරදි ආරම්භය ලබා ගන්න
        2️⃣ කාලය සහ ශ්‍රමය ඉතිරි කර ගන්න
        3️⃣ අලුත්ම ක්‍රමවේද භාවිතා කරන්න
        
        ඔබේ අදහස් සහ අත්දැකීම් Comment කරන්න. පසුව නැරඹීමට Save කර තබා ගන්න! 🔖
        
        ${igTags.joinToString(" ")}
        """.trimIndent()

        val thPost = "$topicSnippet ගැන ඔබේ අදහස මොකක්ද? මම අත්හදා බැලූ දේවල් වලින් හොඳම ප්‍රතිඵල ලැබුණා. සාකච්ඡා කරමු! 👇 ${thTags.joinToString(" ")}"

        val liPost = """
        වර්තමාන ඩිජිටල් පරිසරය තුළ නවෝත්පාදනය සහ කාර්යක්ෂමතාව ඉතා වැදගත්.
        
        මෑතකදී මම $topicSnippet සම්බන්ධයෙන් අධ්‍යයනය කර ප්‍රායෝගිකව ක්‍රියාත්මක කළා.
        
        ප්‍රධාන ප්‍රතිලාභ 3:
        • ඉහළ කාර්යක්ෂමතාව සහ වේගවත් ප්‍රතිඵල
        • පිරිවැය සහ කාලය අවම කර ගැනීම
        • ගුණාත්මකභාවය ඉහළ නැංවීම
        
        ඔබේ වෘත්තීය අත්දැකීම් සහ අදහස් බෙදාගන්න.
        
        ${liTags.joinToString(" ")}
        """.trimIndent()

        val fbPost = """
        යාලුවනේ, අද මම ඔයාලට ගෙනාවේ ගොඩක් වැදගත් මාතෘකාවක්! 💡
        
        $topicSnippet
        
        මේකෙන් ඔයාලට ලොකු ප්‍රයෝජනයක් ගන්න පුළුවන්. අනිවාර්යයෙන්ම බලලා ඔයාලගේ අදහසත් කියන්න. ප්‍රයෝජනවත් නම් යාලුවන්ටත් Share කරන්න! ❤️
        
        ${fbTags.joinToString(" ")}
        """.trimIndent()

        val xPost = "$topicSnippet ගැන සරලව: නිවැරදි ක්‍රමවේදය අනුගමනය කළොත් විශාල වෙනසක් කරන්න පුළුවන්. ඔබේ අදහස පහළින් 👇 ${xTags.joinToString(" ")}"

        return PlatformPackage(
            youtube = YouTubeContent(
                title = ytTitle,
                description = ytDesc,
                hashtags = ytTags,
                videoTags = listOf("Sinhala Tutorial", "Tech Sinhala", "Sri Lanka", "Guide"),
                chaptersSuggestion = listOf("00:00 Intro", "01:15 Step 1", "03:40 Pro Tips", "05:20 Conclusion"),
                cta = cta,
                pinnedComment = "වීඩියෝ එක හොඳයි නම් Like කර යාලුවන්ටත් Share කරන්න! ❤️",
                hookVariants = listOf("මේ රහස ඔයා දැනගෙන හිටියද?", "විනාඩි 2න් මේක ඉගෙන ගන්න!"),
                searchKeywords = listOf("Sinhala video", "How to Sinhala", topicSnippet)
            ),
            tiktok = TikTokContent(
                caption = ttCaption,
                hookText = "විනාඩි 1න් $topicSnippet ඉගෙන ගමු!",
                hashtags = ttTags,
                cta = "Follow කරන්න තවත් වීඩියෝ සඳහා! 🚀",
                searchKeywordPhrases = listOf("Sinhala tips", "Sri Lanka TikTok", topicSnippet),
                onScreenText = listOf("1. Step One", "2. Watch Till End", "3. Results"),
                commentPrompt = "ඔයාලත් මේක කරලා තියෙනවද? Comment කරන්න!"
            ),
            instagram = InstagramContent(
                caption = igCaption,
                firstLineHook = "💡 ඔබත් මේ ක්‍රමය අත්හදා බැලුවද? ($topicSnippet)",
                cta = "Save & Share this Reel!",
                hashtags = igTags,
                coverText = "Secret Guide: $topicSnippet",
                pinnedComment = "වැඩි විස්තර සඳහා Bio එකේ Link එක බලන්න 🔗",
                slideSuggestions = listOf("Slide 1: Problem", "Slide 2: Solution", "Slide 3: Action Plan")
            ),
            threads = ThreadsContent(
                primaryPost = thPost,
                threadContinuation = listOf("පළවෙනි පියවර තමයි නිවැරදි සැලසුම හදාගන්න එක.", "වැඩිදුර විස්තර සඳහා Follow කරන්න!"),
                discussionPrompt = "ඔබ හිතන්නේ කුමක්ද?",
                hashtags = thTags
            ),
            linkedin = LinkedInContent(
                hook = "ඩිජිටල් නවෝත්පාදනය සහ කාර්යක්ෂමතාව: $topicSnippet",
                mainPost = liPost,
                cta = "Let's connect and discuss in comments.",
                hashtags = liTags,
                firstCommentStrategy = "Resources and documentation linked below in comments.",
                postTitle = "Strategic Implementation: $topicSnippet"
            ),
            facebook = FacebookContent(
                mainPost = fbPost,
                shortVersion = "$topicSnippet ගැන සම්පූර්ණ විස්තරය! අදහස් දක්වන්න.",
                cta = "Like & Share කරන්න!",
                hashtags = fbTags,
                firstComment = "ප්‍රශ්න තියෙනවා නම් පහළින් අහන්න යාලුවනේ 👇",
                communityQuestion = "ඔබ මේ ගැන කලින් දැනගෙන හිටියද?"
            ),
            x = XContent(
                standardPost = if (xPost.length > 280) xPost.substring(0, 275) + "..." else xPost,
                alternativeVersion = "$topicSnippet: Smart strategies to get 10x output. Thoughts? 🧵",
                thread = listOf("1/3 Here is how it works...", "2/3 The biggest secret...", "3/3 Follow for more insights."),
                cta = "Retweet & Follow!",
                hashtags = xTags
            )
        )
    }

    private fun generateMixedPackage(
        idea: String,
        contentType: ContentType,
        variation: VariationType,
        ytTags: List<String>,
        ttTags: List<String>,
        igTags: List<String>,
        thTags: List<String>,
        liTags: List<String>,
        fbTags: List<String>,
        xTags: List<String>,
        ctaOverride: String?
    ): PlatformPackage {
        val topicSnippet = idea.take(60)
        val cta = ctaOverride ?: "Comment your thoughts below & Subscribe for more!"

        val ytTitle = when (variation) {
            VariationType.A -> "How to do $topicSnippet (Sinhala Guide)"
            VariationType.B -> "This Changed Everything: $topicSnippet Explained"
            VariationType.C -> "Complete $topicSnippet Step-by-Step Tutorial (Sinhala)"
        }

        val ytDesc = """
        🚀 In this video: $idea
        
        Today we are breaking down $topicSnippet in an easy step-by-step Sinhala + English breakdown.
        
        Key points covered:
        • Complete setup & workflow
        • Best tools & recommendations
        • Common mistakes to avoid
        
        ⏱️ Chapters:
        00:00 - Introduction
        01:30 - Step by Step Walkthrough
        04:10 - Pro Tips & Tricks
        06:00 - Final Thoughts & CTA
        
        🔔 $cta
        
        ${ytTags.joinToString(" ")}
        """.trimIndent()

        val ttCaption = "Quick guide on $topicSnippet! ⚡ ඔයාලත් try කරලා බලන්න. What do you think? Drop a comment! 👇 ${ttTags.joinToString(" ")}"

        val igCaption = """
        🔥 Everything you need to know about $topicSnippet!
        
        Here is the breakdown:
        👉 Easy to set up
        👉 High performance & fast results
        👉 Perfect for creators and builders
        
        Save this post for later and share with someone who needs this! 📌
        
        ${igTags.joinToString(" ")}
        """.trimIndent()

        val thPost = "Thinking about $topicSnippet. Tried this recently and the results were amazing. Have you guys tested this yet? 👇 ${thTags.joinToString(" ")}"

        val liPost = """
        Leveraging modern technology to optimize workflows: A case on $topicSnippet.
        
        Key takeaways:
        1. Speed of execution matters more than perfection.
        2. Combining the right tools creates a huge competitive edge.
        3. Continuous learning and testing drives real growth.
        
        What are your thoughts on this approach?
        
        ${liTags.joinToString(" ")}
        """.trimIndent()

        val fbPost = """
        Hey everyone! 👋 Here is a super useful breakdown on $topicSnippet.
        
        If you are looking to save time and get 10x better results, make sure to check this out!
        
        Let me know your thoughts in the comments! 💬
        
        ${fbTags.joinToString(" ")}
        """.trimIndent()

        val xPost = "Quick breakdown of $topicSnippet: High leverage, simple setup, and fast output. What’s your experience with this? 👇 ${xTags.joinToString(" ")}"

        return PlatformPackage(
            youtube = YouTubeContent(
                title = ytTitle,
                description = ytDesc,
                hashtags = ytTags,
                videoTags = listOf("Tutorial", "Tech Guide", "Creator Tips", "Sinhala Tech"),
                chaptersSuggestion = listOf("00:00 Intro", "01:30 Setup", "04:10 Pro Tips", "06:00 Conclusion"),
                cta = cta,
                pinnedComment = "Drop any questions below in the comments! 👇",
                hookVariants = listOf("Stop doing this the hard way!", "The fastest way to build this:"),
                searchKeywords = listOf("Sinhala tutorial", topicSnippet, "How to guide")
            ),
            tiktok = TikTokContent(
                caption = ttCaption,
                hookText = "How to $topicSnippet in 60s ⚡",
                hashtags = ttTags,
                cta = "Follow for daily tips! 🚀",
                searchKeywordPhrases = listOf("tech tips", "creator hacks", topicSnippet),
                onScreenText = listOf("Step 1: Setup", "Step 2: Execute", "Step 3: Win"),
                commentPrompt = "Have you tried this yet?"
            ),
            instagram = InstagramContent(
                caption = igCaption,
                firstLineHook = "🔥 The smart way to handle $topicSnippet",
                cta = "Save & Share this Reel!",
                hashtags = igTags,
                coverText = "Master $topicSnippet",
                pinnedComment = "Check the link in bio for resources! 🔗",
                slideSuggestions = listOf("Slide 1: Overview", "Slide 2: Step Guide", "Slide 3: Summary")
            ),
            threads = ThreadsContent(
                primaryPost = thPost,
                threadContinuation = listOf("The biggest gamechanger is tool automation.", "Let's connect!"),
                discussionPrompt = "What do you think?",
                hashtags = thTags
            ),
            linkedin = LinkedInContent(
                hook = "Strategic efficiency breakdown: $topicSnippet",
                mainPost = liPost,
                cta = "Share your thoughts in the comments.",
                hashtags = liTags,
                firstCommentStrategy = "Detailed resources and link in the first comment.",
                postTitle = "Insights on $topicSnippet"
            ),
            facebook = FacebookContent(
                mainPost = fbPost,
                shortVersion = "Quick tips on $topicSnippet. Comment below!",
                cta = "Like & Share!",
                hashtags = fbTags,
                firstComment = "Ask any questions below! 👇",
                communityQuestion = "What tool do you use most?"
            ),
            x = XContent(
                standardPost = if (xPost.length > 280) xPost.substring(0, 275) + "..." else xPost,
                alternativeVersion = "$topicSnippet: 3 things you must know before starting. 🧵",
                thread = listOf("1/3 The strategy", "2/3 The execution", "3/3 Final takeaway"),
                cta = "RT & Follow for more!",
                hashtags = xTags
            )
        )
    }

    private fun generateEnglishPackage(
        idea: String,
        contentType: ContentType,
        variation: VariationType,
        ytTags: List<String>,
        ttTags: List<String>,
        igTags: List<String>,
        thTags: List<String>,
        liTags: List<String>,
        fbTags: List<String>,
        xTags: List<String>,
        ctaOverride: String?
    ): PlatformPackage {
        val topicSnippet = idea.take(60)
        val cta = ctaOverride ?: "Subscribe for more high-value creator breakdowns!"

        val ytTitle = when (variation) {
            VariationType.A -> "How I Mastered $topicSnippet (Step-by-Step)"
            VariationType.B -> "The Untapped Strategy for $topicSnippet"
            VariationType.C -> "$topicSnippet: Complete Guide & Action Plan"
        }

        val ytDesc = """
        🚀 In this video: $idea
        
        We break down the exact blueprint for $topicSnippet so you can implement it immediately.
        
        What you'll discover:
        • The foundational strategy and setup
        • Step-by-step implementation walkthrough
        • Common pitfalls and how to avoid them
        
        ⏱️ Chapters:
        00:00 - The Core Problem
        01:10 - The Step-by-Step Blueprint
        03:45 - High-Impact Pro Tips
        05:30 - Summary & Next Steps
        
        🔔 $cta
        
        ${ytTags.joinToString(" ")}
        """.trimIndent()

        val ttCaption = "Stop doing $topicSnippet the hard way. Here is the exact framework 🚀 What do you think? 👇 ${ttTags.joinToString(" ")}"

        val igCaption = """
        ✨ The ultimate blueprint for $topicSnippet.
        
        Here is what you need to know:
        1️⃣ Start with clarity on the goal
        2️⃣ Automate the repetitive steps
        3️⃣ Focus on output quality and consistency
        
        Double tap if you found this helpful! Save for later reference 📌
        
        ${igTags.joinToString(" ")}
        """.trimIndent()

        val thPost = "Most people overcomplicate $topicSnippet. Keep it simple, execute fast, and iterate based on real feedback. Agree or disagree? 👇 ${thTags.joinToString(" ")}"

        val liPost = """
        Execution beats theory every single time.
        
        Here is what I learned while analyzing $topicSnippet:
        
        1. Clarity over complexity: Simplifying the process yields 3x faster turnaround.
        2. Systems over motivation: Consistency comes from built-in habits and workflows.
        3. Actionable insights: Measure what actually moves the needle.
        
        How are you approaching this in your workflow?
        
        ${liTags.joinToString(" ")}
        """.trimIndent()

        val fbPost = """
        Want to level up your results with $topicSnippet? 💡
        
        Here is a straightforward breakdown that anyone can follow to get started today.
        
        Drop a comment with your thoughts and share with a friend who needs this! 🙌
        
        ${fbTags.joinToString(" ")}
        """.trimIndent()

        val xPost = "$topicSnippet in 3 sentences: 1. Keep the workflow simple. 2. Focus on high leverage. 3. Iterate rapidly. Thoughts? 👇 ${xTags.joinToString(" ")}"

        return PlatformPackage(
            youtube = YouTubeContent(
                title = ytTitle,
                description = ytDesc,
                hashtags = ytTags,
                videoTags = listOf("Tutorial", "Guide", "Creator Tips", "Productivity"),
                chaptersSuggestion = listOf("00:00 Intro", "01:10 Blueprint", "03:45 Pro Tips", "05:30 Summary"),
                cta = cta,
                pinnedComment = "What was your biggest takeaway from this video? Let me know below! 👇",
                hookVariants = listOf("The biggest mistake people make with this:", "How to get 10x better results:"),
                searchKeywords = listOf("Tutorial", topicSnippet, "Best practices")
            ),
            tiktok = TikTokContent(
                caption = ttCaption,
                hookText = "The secret to $topicSnippet 🤫",
                hashtags = ttTags,
                cta = "Follow for more creator secrets! 🚀",
                searchKeywordPhrases = listOf("creator tips", "workflow hacks", topicSnippet),
                onScreenText = listOf("1. The Setup", "2. The Framework", "3. The Result"),
                commentPrompt = "Have you tested this framework yet?"
            ),
            instagram = InstagramContent(
                caption = igCaption,
                firstLineHook = "💡 The framework for $topicSnippet that works.",
                cta = "Save & Share this Reel!",
                hashtags = igTags,
                coverText = "Framework: $topicSnippet",
                pinnedComment = "Link in bio for full resources and templates! 🔗",
                slideSuggestions = listOf("Slide 1: The Bottleneck", "Slide 2: The Solution", "Slide 3: Action Steps")
            ),
            threads = ThreadsContent(
                primaryPost = thPost,
                threadContinuation = listOf("The key is focusing on high leverage tasks.", "Let's discuss below!"),
                discussionPrompt = "What is your take?",
                hashtags = thTags
            ),
            linkedin = LinkedInContent(
                hook = "How to approach $topicSnippet with maximum clarity and ROI:",
                mainPost = liPost,
                cta = "Join the conversation in the comments.",
                hashtags = liTags,
                firstCommentStrategy = "Full notes and step-by-step breakdown linked below.",
                postTitle = "Framework Analysis: $topicSnippet"
            ),
            facebook = FacebookContent(
                mainPost = fbPost,
                shortVersion = "Quick tips on $topicSnippet. Comment below!",
                cta = "Like & Share!",
                hashtags = fbTags,
                firstComment = "Drop any questions in the comments! 👇",
                communityQuestion = "What is your biggest challenge with this?"
            ),
            x = XContent(
                standardPost = if (xPost.length > 280) xPost.substring(0, 275) + "..." else xPost,
                alternativeVersion = "A simple 3-step masterclass on $topicSnippet 🧵",
                thread = listOf("1/3 Step 1: Framework", "2/3 Step 2: Execution", "3/3 Step 3: Optimization"),
                cta = "RT & Follow for daily insights!",
                hashtags = xTags
            )
        )
    }
}
