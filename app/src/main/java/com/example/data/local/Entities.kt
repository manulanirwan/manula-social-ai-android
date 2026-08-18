package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.domain.model.ContentType
import com.example.domain.model.ContentTone
import com.example.domain.model.ContentGoal
import com.example.domain.model.LanguageOption

@Entity(tableName = "saved_projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val originalIdea: String,
    val contentType: String,
    val language: String,
    val tone: String,
    val goal: String,
    val targetAudience: String,
    val brandName: String,
    val creatorName: String,
    val selectedPlatformsJson: String,
    val versionAJson: String,
    val versionBJson: String,
    val versionCJson: String,
    val qualityScore: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_items")
data class FavoriteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val content: String,
    val category: String, // CAPTION, TITLE, HOOK, HASHTAGS, FULL_PACKAGE
    val platform: String, // YOUTUBE, TIKTOK, etc. or ALL
    val sourceIdea: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "brand_profiles")
data class BrandProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val description: String,
    val website: String,
    val mainTopics: String,
    val preferredLanguage: String,
    val preferredTone: String,
    val targetAudience: String,
    val defaultCta: String,
    val brandHashtagsJson: String,
    val emojiUsage: String,
    val isActive: Boolean
)
