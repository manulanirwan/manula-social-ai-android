package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectDao {
    @Query("SELECT * FROM saved_projects ORDER BY updatedAt DESC")
    fun getAllProjects(): Flow<List<ProjectEntity>>

    @Query("SELECT * FROM saved_projects WHERE id = :id")
    suspend fun getProjectById(id: Long): ProjectEntity?

    @Query("SELECT * FROM saved_projects WHERE title LIKE '%' || :query || '%' OR originalIdea LIKE '%' || :query || '%' ORDER BY updatedAt DESC")
    fun searchProjects(query: String): Flow<List<ProjectEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProject(project: ProjectEntity): Long

    @Update
    suspend fun updateProject(project: ProjectEntity)

    @Query("DELETE FROM saved_projects WHERE id = :id")
    suspend fun deleteProjectById(id: Long)

    @Query("DELETE FROM saved_projects")
    suspend fun clearAllProjects()
}

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite_items ORDER BY createdAt DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT * FROM favorite_items WHERE category = :category ORDER BY createdAt DESC")
    fun getFavoritesByCategory(category: String): Flow<List<FavoriteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(favorite: FavoriteEntity): Long

    @Query("DELETE FROM favorite_items WHERE id = :id")
    suspend fun deleteFavoriteById(id: Long)

    @Query("DELETE FROM favorite_items")
    suspend fun clearAllFavorites()
}

@Dao
interface BrandProfileDao {
    @Query("SELECT * FROM brand_profiles ORDER BY id ASC")
    fun getAllProfiles(): Flow<List<BrandProfileEntity>>

    @Query("SELECT * FROM brand_profiles WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveProfile(): BrandProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: BrandProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: BrandProfileEntity)

    @Query("UPDATE brand_profiles SET isActive = CASE WHEN id = :activeId THEN 1 ELSE 0 END")
    suspend fun setActiveProfile(activeId: Long)

    @Query("DELETE FROM brand_profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Long)
}
