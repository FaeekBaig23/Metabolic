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
    private val userId get() = auth.currentUser?.uid ?: ""

    // ── Get profile as Flow (live updates) ───────────────
    fun getProfile(): Flow<UserProfileEntity?> =
        dao.getProfile(userId)

    // ── Get profile once ──────────────────────────────────
    suspend fun getProfileOnce(): UserProfileEntity? =
        dao.getProfileOnce(userId)

    // ── Save profile locally + sync to Firestore ──────────
    suspend fun saveProfile(profile: UserProfileEntity) {
        dao.insertProfile(profile)
        syncToFirestore(profile)
    }

    // ── Sync to Firestore ─────────────────────────────────
    private suspend fun syncToFirestore(profile: UserProfileEntity) {
        try {
            firestore.collection("users")
                .document(userId)
                .set(profile)
                .await()
        } catch (e: Exception) {
            // Sync failed — local data is still saved
        }
    }

    // ── Check if profile exists ───────────────────────────
    suspend fun hasProfile(): Boolean =
        dao.getProfileOnce(userId) != null
}