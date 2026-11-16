package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class FirestoreRulesTest : FirestoreEmulatorTestBase() {

    @Test
    fun userProfile_authenticatedOwner_canReadAndWrite() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val userDoc = firestore.collection("users").document(uid)

        val profileData = mapOf(
            "userId" to uid,
            "username" to "AliceRunner",
            "weed" to 500L
        )

        withTimeout(DEFAULT_TIMEOUT_MS) {
            userDoc.set(profileData).await()
        }

        val snapshot = withTimeout(DEFAULT_TIMEOUT_MS) {
            userDoc.get().await()
        }

        assertTrue(snapshot.exists())
        assertEquals("AliceRunner", snapshot.getString("username"))
        assertEquals(500L, snapshot.getLong("weed"))
    }

    @Test
    fun userProfile_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceDoc = firestore.collection("users").document(aliceUid)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceDoc.set(mapOf("userId" to aliceUid, "username" to "Alice", "weed" to 100L)).await()
        }

        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                aliceDoc.get().await()
            }
            fail("Expected PERMISSION_DENIED when Bob reads Alice's profile")
        } catch (e: Exception) {
            val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
        }
    }

    @Test
    fun userProfile_unauthenticated_failsWithPermissionDenied() = runBlocking {
        val uid = "random_user_${UUID.randomUUID()}"
        auth.signOut()

        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(uid).get().await()
            }
            fail("Expected PERMISSION_DENIED for unauthenticated read")
        } catch (e: Exception) {
            val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
        }
    }

    @Test
    fun readings_authenticatedOwner_canSaveAndRead() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val readingId = "reading_${UUID.randomUUID()}"
        val readingDoc = firestore.collection("users").document(uid).collection("readings").document(readingId)

        val readingPayload = mapOf(
            "id" to readingId,
            "userId" to uid,
            "question" to "Will the grid stabilize?",
            "cardIndices" to listOf(0, 1, 2),
            "isUpright" to listOf(true, true, false),
            "report" to "Grid integrity 98%",
            "timestamp" to System.currentTimeMillis()
        )

        withTimeout(DEFAULT_TIMEOUT_MS) {
            readingDoc.set(readingPayload).await()
        }

        val snapshot = withTimeout(DEFAULT_TIMEOUT_MS) {
            readingDoc.get().await()
        }

        assertTrue(snapshot.exists())
        assertEquals("Will the grid stabilize?", snapshot.getString("question"))
    }

    @Test
    fun userProfile_clientCannotInflateWeedOnUpdate() = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val userDoc = firestore.collection("users").document(uid)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            userDoc.set(mapOf("userId" to uid, "username" to "AliceRunner", "weed" to 500L)).await()
        }

        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                userDoc.update("weed", 999999L).await()
            }
            fail("Expected PERMISSION_DENIED when client attempts to inflate weed")
        } catch (e: Exception) {
            val firestoreException = checkNotNull(e.unwrapFirestoreException()) {
                "Expected nested FirebaseFirestoreException, got $e"
            }
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreException.code)
        }
    }

    @Test
    fun market_buyerCanPurchaseUnsoldListing_withoutPriceTampering() = runBlocking {
        val sellerUid = signInTestUser(ALICE_EMAIL)
        val listingId = "listing_${UUID.randomUUID()}"
        val listingDoc = firestore.collection("market").document(listingId)

        withTimeout(DEFAULT_TIMEOUT_MS) {
            listingDoc.set(
                mapOf(
                    "id" to listingId,
                    "sellerId" to sellerUid,
                    "cardIndex" to 5L,
                    "price" to 250L,
                    "timestamp" to System.currentTimeMillis(),
                    "isSold" to false
                )
            ).await()
        }

        val buyerUid = signInTestUser(BOB_EMAIL)
        withTimeout(DEFAULT_TIMEOUT_MS) {
            listingDoc.update(mapOf("isSold" to true, "buyerId" to buyerUid)).await()
        }

        val updated = withTimeout(DEFAULT_TIMEOUT_MS) { listingDoc.get().await() }
        assertEquals(true, updated.getBoolean("isSold"))
        assertEquals(buyerUid, updated.getString("buyerId"))
    }

    private companion object {
        const val ALICE_EMAIL = "alice_rules@test.com"
        const val BOB_EMAIL = "bob_rules@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
    }
}
