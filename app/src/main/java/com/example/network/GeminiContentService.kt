package com.example.network

import com.example.domain.model.*
import com.example.engine.HashtagEngine
import org.json.JSONArray
import org.json.JSONObject
import retrofit2.HttpException
import java.io.IOException

class GeminiApiKeyMissingException(message: String) : Exception(message)
class GeminiApiException(message: String, val statusCode: Int? = null) : Exception(message)

class GeminiContentService(
    private val apiService: GeminiApiService = RetrofitClient.geminiService
) {
    val selectedModel: String = GeminiConstants.DEFAULT_MODEL

    /**
     * Tests connection with the Gemini 3.6 Flash API using the provided key.
     */
    suspend fun testConnection(apiKey: String): Result<String> {
        val trimmedKey = apiKey.trim()
        if (trimmedKey.isBlank()) {
            return Result.failure(GeminiApiKeyMissingException("API key cannot be empty."))
        }

        return try {
            val testRequest = GeminiRequest(
                contents = listOf(
                    ContentItem(parts = listOf(PartItem(text = "Respond with JSON: {\"status\": \"ok\"}")))
                ),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json"
                ),
                systemInstruction = ContentItem(
                    parts = listOf(PartItem(text = "You are a test helper. Respond only in valid JSON."))
                )
            )

            val response = apiService.generateContent(
                model = selectedModel,
                apiKey = trimmedKey,
                request = testRequest
            )

            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!text.isNullOrBlank()) {
                Result.success("Gemini API connection successful ($selectedModel).")
            } else {
                Result.failure(GeminiApiException("Empty response received from Gemini API."))
            }
        } catch (e: HttpException) {
            val errorMsg = mapHttpError(e.code(), e.message())
            Result.failure(GeminiApiException(errorMsg, e.code()))
        } catch (e: IOException) {
            Result.failure(GeminiApiException("Unable to connect to Gemini API. Please check your internet connection."))
        } catch (e: Exception) {
            Result.failure(GeminiApiException("Connection test failed: ${e.localizedMessage ?: "Unknown error"}"))
        }
    }

    /**
     * Generates a complete platform package using real Gemini 3.6 Flash.
     */
    suspend fun generateSocialPackages(
        apiKey: String?,
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
        if (apiKey.isNullOrBlank()) {
            throw GeminiApiKeyMissingException(
                "No Gemini API key found. Please open Settings -> Gemini API to add your API key."
            )
        }

        try {
            val prompt = buildFullPackagePrompt(
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
                    responseMimeType = "application/json",
                    thinkingConfig = ThinkingConfig(thinkingLevel = "medium")
                ),
                systemInstruction = ContentItem(
                    parts = listOf(
                        PartItem(text = GeminiConstants.SYSTEM_INSTRUCTION)
                    )
                )
            )

            val response = apiService.generateContent(
                model = selectedModel,
                apiKey = apiKey,
                request = request
            )

            val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (rawJson.isNullOrBlank()) {
                throw GeminiApiException("Received empty response from Gemini API.")
            }

            return parsePlatformPackage(rawJson, idea, brandProfile, selectedPlatforms)
        } catch (e: HttpException) {
            throw GeminiApiException(mapHttpError(e.code(), e.message()), e.code())
        } catch (e: IOException) {
            throw GeminiApiException("No internet connection. Unable to reach Gemini API.")
        } catch (e: GeminiApiException) {
            throw e
        } catch (e: GeminiApiKeyMissingException) {
            throw e
        } catch (e: Exception) {
            throw GeminiApiException("AI Generation failed: ${e.localizedMessage ?: "Please try again."}")
        }
    }

    /**
     * Regenerates a single field (e.g. YouTube title, TikTok caption, X post) via targeted Gemini 3.6 Flash call.
     */
    suspend fun regenerateField(
        apiKey: String?,
        platform: SocialPlatform,
        fieldName: String,
        currentContent: String,
        originalIdea: String,
        language: LanguageOption,
        tone: ContentTone,
        brandProfile: BrandProfile?,
        instruction: String
    ): String {
        if (apiKey.isNullOrBlank()) {
            throw GeminiApiKeyMissingException("Please configure your Gemini API key in Settings.")
        }

        try {
            val prompt = """
            You are refining a single field for a social media post.
            
            Platform: ${platform.displayName}
            Field: $fieldName
            Original Topic / Idea: "$originalIdea"
            Current Content: "$currentContent"
            Language Mode: ${language.displayName}
            Tone: ${tone.displayName}
            ${if (brandProfile != null && brandProfile.name.isNotBlank()) "Brand: ${brandProfile.name} (${brandProfile.description})" else ""}
            
            Instruction for regeneration: $instruction
            
            Output JSON only with a single string property "updatedContent":
            {"updatedContent": "..."}
            """.trimIndent()

            val request = GeminiRequest(
                contents = listOf(ContentItem(parts = listOf(PartItem(text = prompt)))),
                generationConfig = GenerationConfig(
                    responseMimeType = "application/json"
                ),
                systemInstruction = ContentItem(
                    parts = listOf(PartItem(text = GeminiConstants.SYSTEM_INSTRUCTION))
                )
            )

            val response = apiService.generateContent(
                model = selectedModel,
                apiKey = apiKey,
                request = request
            )

            val rawJson = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
            if (!rawJson.isNullOrBlank()) {
                val clean = rawJson.replace("```json", "").replace("```", "").trim()
                val json = JSONObject(clean)
                val updated = json.optString("updatedContent")
                if (updated.isNotBlank()) return updated
            }
            return currentContent
        } catch (e: Exception) {
            throw GeminiApiException("Field regeneration failed: ${e.localizedMessage}")
        }
    }

    private fun mapHttpError(code: Int, serverMessage: String?): String {
        return when (code) {
            400 -> "Invalid request format to Gemini API (HTTP 400)."
            401, 403 -> "Invalid Gemini API key (HTTP $code). Please check your key in Settings -> Gemini API."
            404 -> "Gemini 3.6 Flash model is currently unavailable (HTTP 404)."
            429 -> "Gemini API rate limit reached (HTTP 429). Please wait a moment and try again."
            500, 502, 503 -> "Gemini API service is temporarily unavailable (HTTP $code). Please try again shortly."
            else -> "Gemini API error (HTTP $code): ${serverMessage ?: "Check your connection and credentials."}"
        }
    }

    private fun buildFullPackagePrompt(
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
            BRAND PROFILE:
            - Name: ${brandProfile.name}
            - Description: ${brandProfile.description}
            - Default Call to Action: ${brandProfile.defaultCta}
            - Brand Hashtags: ${brandProfile.brandHashtags.joinToString(" ")}
            - Preferred Emojis: ${brandProfile.emojiUsage}
            """.trimIndent()
        } else ""

        val languageGuide = when (language) {
            LanguageOption.SINHALA -> "Strictly write in natural conversational Sinhala (සිංහල). Avoid stiff literal translations; use authentic Sri Lankan phrasing."
            LanguageOption.MIXED -> "Write in natural Sri Lankan Sinhala-English mixed creator language (Singlish / Sinhala + English terms)."
            LanguageOption.ENGLISH -> "Write in fluent, high-engagement modern English."
            LanguageOption.AUTO -> "Detect the input language and style (Sinhala, English, or Mixed) and match it with native fluency."
        }

        val platformsRequested = selectedPlatforms.joinToString(", ") { it.displayName }

        return """
        Generate an elite multi-platform publishing package from the following concept.
        
        CREATOR CONCEPT / INPUT:
        "$idea"
        
        SPECIFICATIONS:
        - Content Type: ${contentType.displayName} (${contentType.description})
        - Language Directive: $languageGuide
        - Tone: ${tone.displayName}
        - Primary Goal: ${goal.displayName}
        - Target Audience: ${if (targetAudience.isNotBlank()) targetAudience else "General Social Audience"}
        - Variation Angle: ${variationType.label} (${variationType.subtitle})
        ${if (!regenInstruction.isNullOrBlank()) "- Specific Adjustment: $regenInstruction" else ""}
        $brandContext
        
        PLATFORMS TO GENERATE: $platformsRequested
        
        PLATFORM RULES & EXPECTATIONS:
        1. YouTube:
           - title: Front-load topic keywords. Max 100 characters (recommended 45-70).
           - description: Hook in first 3 lines, clear summary, timestamps/chapters, CTA, and 3-5 hashtags.
           - hashtags: 3-5 relevant tags.
           - videoTags: Comma-separated search tags list.
           - chaptersSuggestion: 3-5 structured timestamp chapters.
           - cta: Specific viewer engagement CTA.
           - pinnedComment: Engaging question or resource pin.
           - hookVariants: 2 alternative hook ideas.
           - searchKeywords: 3-5 SEO search terms.
        2. TikTok:
           - hookText: Punchy 0-3s visual or text hook.
           - caption: Native creator caption under 300 characters.
           - hashtags: 3-6 niche tags.
           - cta: Interactive follow or comment prompt.
           - searchKeywordPhrases: 3 SEO keywords.
           - onScreenText: 3 brief on-screen text overlays.
           - commentPrompt: Question to drive comments.
        3. Instagram (Reels & Posts):
           - firstLineHook: High-curiosity first line before "...more" fold.
           - caption: Clean line breaks, value delivery, CTA.
           - hashtags: 5-8 targeted hashtags.
           - coverText: Reel cover headline.
           - pinnedComment: CTA comment.
           - slideSuggestions: 3 slide breakdown ideas if carousel.
        4. Threads:
           - primaryPost: Conversational, opinion-oriented, under 500 characters.
           - threadContinuation: 2 follow-up thoughts.
           - discussionPrompt: Open-ended question.
           - hashtags: 1-2 topic tags.
        5. LinkedIn:
           - hook: Compelling first 2 lines.
           - mainPost: Professional insight, generous line breaks (1-2 sentences), actionable lesson.
           - cta: Professional discussion CTA.
           - hashtags: 3-5 industry tags.
           - firstCommentStrategy: Strategy or link note for comment 1.
           - postTitle: Headline title.
        6. Facebook:
           - mainPost: Warm, relatable community post.
           - shortVersion: Snappy alternative for quick scanning.
           - cta: Like, share, or comment CTA.
           - hashtags: 2-4 tags.
           - firstComment: Prompt for discussion.
           - communityQuestion: Relatable question.
        7. X (Twitter):
           - standardPost: STRICTLY under 280 characters. High punch per word.
           - alternativeVersion: Second angle under 280 characters.
           - thread: 3 connected tweets (1/3, 2/3, 3/3).
           - cta: Retweet or follow call.
           - hashtags: 1-2 tags.
        
        OUTPUT FORMAT: Return ONLY a valid JSON object strictly matching this schema:
        {
          "youtube": {
            "title": "...",
            "description": "...",
            "hashtags": ["#tag1", "#tag2"],
            "videoTags": ["tag1", "tag2"],
            "chaptersSuggestion": ["00:00 Intro", "01:00 Setup", "03:00 Conclusion"],
            "cta": "...",
            "pinnedComment": "...",
            "hookVariants": ["Hook 1", "Hook 2"],
            "searchKeywords": ["keyword1", "keyword2"]
          },
          "tiktok": {
            "caption": "...",
            "hookText": "...",
            "hashtags": ["#tag1", "#tag2"],
            "cta": "...",
            "searchKeywordPhrases": ["phrase1", "phrase2"],
            "onScreenText": ["Text 1", "Text 2"],
            "commentPrompt": "..."
          },
          "instagram": {
            "caption": "...",
            "firstLineHook": "...",
            "cta": "...",
            "hashtags": ["#tag1", "#tag2"],
            "coverText": "...",
            "pinnedComment": "...",
            "slideSuggestions": ["Slide 1", "Slide 2", "Slide 3"]
          },
          "threads": {
            "primaryPost": "...",
            "threadContinuation": ["Post 2", "Post 3"],
            "discussionPrompt": "...",
            "hashtags": ["#tag1"]
          },
          "linkedin": {
            "hook": "...",
            "mainPost": "...",
            "cta": "...",
            "hashtags": ["#tag1", "#tag2"],
            "firstCommentStrategy": "...",
            "postTitle": "..."
          },
          "facebook": {
            "mainPost": "...",
            "shortVersion": "...",
            "cta": "...",
            "hashtags": ["#tag1", "#tag2"],
            "firstComment": "...",
            "communityQuestion": "..."
          },
          "x": {
            "standardPost": "...",
            "alternativeVersion": "...",
            "thread": ["1/3 ...", "2/3 ...", "3/3 ..."],
            "cta": "...",
            "hashtags": ["#tag1"]
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

        val root = try {
            JSONObject(cleanJson)
        } catch (e: Exception) {
            // Attempt to find the first '{' and last '}'
            val firstBrace = cleanJson.indexOf('{')
            val lastBrace = cleanJson.lastIndexOf('}')
            if (firstBrace != -1 && lastBrace != -1 && lastBrace > firstBrace) {
                JSONObject(cleanJson.substring(firstBrace, lastBrace + 1))
            } else {
                throw GeminiApiException("Invalid JSON response from Gemini API.")
            }
        }

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
}
