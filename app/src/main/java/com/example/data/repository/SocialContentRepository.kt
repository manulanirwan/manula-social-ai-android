package com.example.data.repository

import android.content.Context
import com.example.data.local.*
import com.example.data.security.SecureApiKeyStorage
import com.example.domain.model.*
import com.example.engine.ContentQualityEngine
import com.example.network.GeminiContentService
import com.example.network.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class SocialContentRepository(
    private val projectDao: ProjectDao,
    private val favoriteDao: FavoriteDao,
    private val brandProfileDao: BrandProfileDao,
    private val secureStorage: SecureApiKeyStorage,
    private val geminiService: GeminiContentService = GeminiContentService()
) {
    private val moshi = RetrofitClient.moshi
    private val platformPackageAdapter = moshi.adapter(PlatformPackage::class.java)

    companion object {
        @Volatile
        private var INSTANCE: SocialContentRepository? = null

        fun getInstance(context: Context): SocialContentRepository {
            return INSTANCE ?: synchronized(this) {
                val db = AppDatabase.getDatabase(context)
                val secureStorage = SecureApiKeyStorage.getInstance(context)
                val repo = SocialContentRepository(
                    projectDao = db.projectDao(),
                    favoriteDao = db.favoriteDao(),
                    brandProfileDao = db.brandProfileDao(),
                    secureStorage = secureStorage
                )
                INSTANCE = repo
                repo
            }
        }
    }

    // API Key Management
    fun getApiKey(): String? = secureStorage.getApiKey()
    fun hasApiKey(): Boolean = secureStorage.hasApiKey()
    fun getMaskedApiKey(): String = secureStorage.getMaskedApiKey()
    fun saveApiKey(key: String): Boolean = secureStorage.saveApiKey(key)
    fun removeApiKey() = secureStorage.removeApiKey()
    fun isCustomKeySet(): Boolean = secureStorage.isCustomKeySet()

    suspend fun testApiKey(key: String): Result<String> = withContext(Dispatchers.IO) {
        geminiService.testConnection(key)
    }

    suspend fun generateCompletePackage(
        idea: String,
        contentType: ContentType,
        language: LanguageOption,
        tone: ContentTone,
        goal: ContentGoal,
        targetAudience: String,
        brandProfile: BrandProfile?,
        selectedPlatforms: Set<SocialPlatform>
    ): SocialPackage = withContext(Dispatchers.IO) {
        val apiKey = secureStorage.getApiKey()

        // Generate Version A
        val vA = geminiService.generateSocialPackages(
            apiKey = apiKey,
            idea = idea,
            contentType = contentType,
            language = language,
            tone = tone,
            goal = goal,
            targetAudience = targetAudience,
            brandProfile = brandProfile,
            selectedPlatforms = selectedPlatforms,
            variationType = VariationType.A
        )

        // Generate Version B (Higher Curiosity / Punchy)
        val vB = geminiService.generateSocialPackages(
            apiKey = apiKey,
            idea = idea,
            contentType = contentType,
            language = language,
            tone = tone,
            goal = goal,
            targetAudience = targetAudience,
            brandProfile = brandProfile,
            selectedPlatforms = selectedPlatforms,
            variationType = VariationType.B
        )

        // Generate Version C (Educational & Direct Value)
        val vC = geminiService.generateSocialPackages(
            apiKey = apiKey,
            idea = idea,
            contentType = contentType,
            language = language,
            tone = tone,
            goal = goal,
            targetAudience = targetAudience,
            brandProfile = brandProfile,
            selectedPlatforms = selectedPlatforms,
            variationType = VariationType.C
        )

        val validation = ContentQualityEngine.evaluatePackage(vA, selectedPlatforms)

        SocialPackage(
            originalIdea = idea,
            contentType = contentType,
            language = language,
            tone = tone,
            goal = goal,
            targetAudience = targetAudience,
            brandName = brandProfile?.name.orEmpty(),
            creatorName = "",
            productName = "",
            websiteUrl = brandProfile?.website.orEmpty(),
            mainKeyword = "",
            location = "",
            selectedPlatforms = selectedPlatforms,
            versionA = vA,
            versionB = vB,
            versionC = vC,
            aiSuggestions = validation.suggestions,
            qualityScore = validation.qualityScore
        )
    }

    suspend fun regeneratePlatformContent(
        idea: String,
        contentType: ContentType,
        language: LanguageOption,
        tone: ContentTone,
        goal: ContentGoal,
        targetAudience: String,
        brandProfile: BrandProfile?,
        platform: SocialPlatform,
        currentPkg: PlatformPackage,
        styleInstruction: String
    ): PlatformPackage = withContext(Dispatchers.IO) {
        val apiKey = secureStorage.getApiKey()

        val newPkg = geminiService.generateSocialPackages(
            apiKey = apiKey,
            idea = idea,
            contentType = contentType,
            language = language,
            tone = tone,
            goal = goal,
            targetAudience = targetAudience,
            brandProfile = brandProfile,
            selectedPlatforms = setOf(platform),
            variationType = VariationType.A,
            regenInstruction = styleInstruction
        )

        when (platform) {
            SocialPlatform.YOUTUBE -> currentPkg.copy(youtube = newPkg.youtube)
            SocialPlatform.TIKTOK -> currentPkg.copy(tiktok = newPkg.tiktok)
            SocialPlatform.INSTAGRAM -> currentPkg.copy(instagram = newPkg.instagram)
            SocialPlatform.THREADS -> currentPkg.copy(threads = newPkg.threads)
            SocialPlatform.LINKEDIN -> currentPkg.copy(linkedin = newPkg.linkedin)
            SocialPlatform.FACEBOOK -> currentPkg.copy(facebook = newPkg.facebook)
            SocialPlatform.X -> currentPkg.copy(x = newPkg.x)
        }
    }

    suspend fun regenerateSingleField(
        platform: SocialPlatform,
        fieldName: String,
        currentContent: String,
        originalIdea: String,
        language: LanguageOption,
        tone: ContentTone,
        brandProfile: BrandProfile?,
        instruction: String
    ): String = withContext(Dispatchers.IO) {
        val apiKey = secureStorage.getApiKey()
        geminiService.regenerateField(
            apiKey = apiKey,
            platform = platform,
            fieldName = fieldName,
            currentContent = currentContent,
            originalIdea = originalIdea,
            language = language,
            tone = tone,
            brandProfile = brandProfile,
            instruction = instruction
        )
    }

    // Projects CRUD
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    suspend fun saveProject(socialPackage: SocialPackage): Long = withContext(Dispatchers.IO) {
        val platformsString = socialPackage.selectedPlatforms.joinToString(",") { it.name }
        val titleSnippet = if (socialPackage.originalIdea.length > 40) {
            socialPackage.originalIdea.take(37) + "..."
        } else {
            socialPackage.originalIdea
        }

        val entity = ProjectEntity(
            title = titleSnippet,
            originalIdea = socialPackage.originalIdea,
            contentType = socialPackage.contentType.name,
            language = socialPackage.language.name,
            tone = socialPackage.tone.name,
            goal = socialPackage.goal.name,
            targetAudience = socialPackage.targetAudience,
            brandName = socialPackage.brandName,
            creatorName = socialPackage.creatorName,
            selectedPlatformsJson = platformsString,
            versionAJson = platformPackageAdapter.toJson(socialPackage.versionA),
            versionBJson = platformPackageAdapter.toJson(socialPackage.versionB),
            versionCJson = platformPackageAdapter.toJson(socialPackage.versionC),
            qualityScore = socialPackage.qualityScore,
            createdAt = socialPackage.createdAt,
            updatedAt = System.currentTimeMillis()
        )
        projectDao.insertProject(entity)
    }

    suspend fun updateProject(project: ProjectEntity) = withContext(Dispatchers.IO) {
        projectDao.updateProject(project.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deleteProject(id: Long) = withContext(Dispatchers.IO) {
        projectDao.deleteProjectById(id)
    }

    suspend fun clearAllProjects() = withContext(Dispatchers.IO) {
        projectDao.clearAllProjects()
    }

    fun searchProjects(query: String): Flow<List<ProjectEntity>> = projectDao.searchProjects(query)

    // Favorites CRUD
    val allFavorites: Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()

    suspend fun addFavorite(
        title: String,
        content: String,
        category: String,
        platform: String,
        sourceIdea: String
    ) = withContext(Dispatchers.IO) {
        val entity = FavoriteEntity(
            title = title,
            content = content,
            category = category,
            platform = platform,
            sourceIdea = sourceIdea
        )
        favoriteDao.insertFavorite(entity)
    }

    suspend fun deleteFavorite(id: Long) = withContext(Dispatchers.IO) {
        favoriteDao.deleteFavoriteById(id)
    }

    suspend fun clearAllFavorites() = withContext(Dispatchers.IO) {
        favoriteDao.clearAllFavorites()
    }

    // Brand Profiles CRUD
    val allBrandProfiles: Flow<List<BrandProfile>> = brandProfileDao.getAllProfiles().map { entities ->
        entities.map { mapEntityToBrandProfile(it) }
    }

    suspend fun getActiveBrandProfile(): BrandProfile? = withContext(Dispatchers.IO) {
        brandProfileDao.getActiveProfile()?.let { mapEntityToBrandProfile(it) }
    }

    suspend fun saveBrandProfile(profile: BrandProfile): Long = withContext(Dispatchers.IO) {
        val entity = BrandProfileEntity(
            id = profile.id,
            name = profile.name,
            description = profile.description,
            website = profile.website,
            mainTopics = profile.mainTopics,
            preferredLanguage = profile.preferredLanguage.name,
            preferredTone = profile.preferredTone.name,
            targetAudience = profile.targetAudience,
            defaultCta = profile.defaultCta,
            brandHashtagsJson = profile.brandHashtags.joinToString(","),
            emojiUsage = profile.emojiUsage,
            isActive = profile.isActive
        )
        if (profile.id == 0L) {
            brandProfileDao.insertProfile(entity)
        } else {
            brandProfileDao.updateProfile(entity)
            profile.id
        }
    }

    suspend fun setActiveBrandProfile(id: Long) = withContext(Dispatchers.IO) {
        brandProfileDao.setActiveProfile(id)
    }

    suspend fun deleteBrandProfile(id: Long) = withContext(Dispatchers.IO) {
        brandProfileDao.deleteProfileById(id)
    }

    private fun mapEntityToBrandProfile(e: BrandProfileEntity): BrandProfile {
        val lang = runCatching { LanguageOption.valueOf(e.preferredLanguage) }.getOrDefault(LanguageOption.AUTO)
        val tone = runCatching { ContentTone.valueOf(e.preferredTone) }.getOrDefault(ContentTone.PROFESSIONAL)
        val tags = if (e.brandHashtagsJson.isNotBlank()) e.brandHashtagsJson.split(",") else emptyList()
        return BrandProfile(
            id = e.id,
            name = e.name,
            description = e.description,
            website = e.website,
            mainTopics = e.mainTopics,
            preferredLanguage = lang,
            preferredTone = tone,
            targetAudience = e.targetAudience,
            defaultCta = e.defaultCta,
            brandHashtags = tags,
            emojiUsage = e.emojiUsage,
            isActive = e.isActive
        )
    }

    fun parseStoredPackage(jsonString: String): PlatformPackage? {
        return runCatching { platformPackageAdapter.fromJson(jsonString) }.getOrNull()
    }
}
