package com.faiqbaig.metabolic.core.data.repository

import com.faiqbaig.metabolic.core.data.local.UserProfileDao
import com.faiqbaig.metabolic.core.data.local.UserProfileEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserProfileRepository @Inject constructor(
    private val dao      : UserProfileDao,
    private val auth     : FirebaseAuth,
    private val firestore: FirebaseFirestore
) {
    // Renamed slightly to avoid conflict with the userId parameter in saveProfile
    private val currentUserId get() = auth.currentUser?.uid ?: ""

    // ── Get profile as Flow (live updates) ───────────────
    fun getProfile(): Flow<UserProfileEntity?> =
        dao.getProfile(currentUserId)

    // ── Get profile once ──────────────────────────────────
    suspend fun getProfileOnce(): UserProfileEntity? =
        dao.getProfileOnce(currentUserId)

    // ── Check if profile exists ───────────────────────────
    suspend fun hasProfile(): Boolean =
        dao.getProfileOnce(currentUserId) != null

    // ── Save profile locally + sync to Firestore ──────────
    suspend fun saveProfile(
        userId: String,
        name: String,
        gender: String,
        age: Int,
        weightKg: Float,
        heightCm: Float,
        goal: String,
        activityLevel: String,
        activityTypes: String,
        dietType: String,
        allergies: String,
        medicalConditions: String,
        risks: String,
        background: String,
        dailyCalorieTarget: Int,
        dailyProteinTarget: Int,
        dailyCarbsTarget: Int,
        dailyFatTarget: Int,
        bmi: Float
    ) {
        val now = System.currentTimeMillis()

        val entity = UserProfileEntity(
            userId              = userId,
            name                = name,
            gender              = gender,
            age                 = age,
            weightKg            = weightKg,
            heightCm            = heightCm,
            goal                = goal,
            activityLevel       = activityLevel,
            activityTypes       = activityTypes,
            dietType            = dietType,
            allergies           = allergies,
            medicalConditions   = medicalConditions,
            risks               = risks,
            background          = background,
            dailyCalorieTarget  = dailyCalorieTarget,
            dailyProteinTarget  = dailyProteinTarget,
            dailyCarbsTarget    = dailyCarbsTarget,
            dailyFatTarget      = dailyFatTarget,
            bmi                 = bmi,
            createdAt           = now,
            updatedAt           = now
        )

        // 1. Save locally to Room
        dao.insertProfile(entity)

        // 2. Sync to Firestore (best-effort — silently ignores offline failures)
        try {
            firestore.collection("users")
                .document(userId)
                .set(entity.toMap())   // Uses the toMap() extension below
                .await()
        } catch (e: Exception) {
            // WorkManager sync can retry this later
        }
    }
}

// ── Helper extension on UserProfileEntity to convert to Firestore map:
private fun UserProfileEntity.toMap(): Map<String, Any?> = mapOf(
    "userId"             to userId,
    "name"               to name,
    "gender"             to gender,
    "age"                to age,
    "weightKg"           to weightKg,
    "heightCm"           to heightCm,
    "goal"               to goal,
    "activityLevel"      to activityLevel,
    "activityTypes"      to activityTypes,
    "dietType"           to dietType,
    "allergies"          to allergies,
    "medicalConditions"  to medicalConditions,
    "risks"              to risks,
    "background"         to background,
    "dailyCalorieTarget" to dailyCalorieTarget,
    "dailyProteinTarget" to dailyProteinTarget,
    "dailyCarbsTarget"   to dailyCarbsTarget,
    "dailyFatTarget"     to dailyFatTarget,
    "bmi"                to bmi,
    "createdAt"          to createdAt,
    "updatedAt"          to updatedAt
)