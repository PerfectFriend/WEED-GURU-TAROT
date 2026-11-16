package com.example.data

import android.app.Application
import android.os.Looper
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import java.io.DataInputStream
import java.io.DataOutputStream
import java.net.Socket

@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AuditSecurityRegressionsTest {

    @Test
    fun c4_swarmStorage_isDisabledByDefault_servesCiphertextOnly_andRejectsUnauthenticatedTcp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val swarm = SwarmStorageManager.getInstance(app)

        try {
            assertFalse("Swarm node must be disabled by default", swarm.nodeActive.value)

            val sizeBytes = 10L * 1024L * 1024L
            var callbackResult: Boolean? = null
            swarm.allocateContainer(sizeBytes, "SwarmTestPass#2026") { ok ->
                callbackResult = ok
            }

            val deadline = System.currentTimeMillis() + 10_000L
            while ((swarm.isAllocating.value || callbackResult == null) && System.currentTimeMillis() < deadline) {
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                Thread.sleep(50)
            }
            assertEquals("Container allocation must succeed", true, callbackResult)
            assertTrue("Mount with correct passphrase must succeed", swarm.mountContainer("SwarmTestPass#2026"))

            val secretBytes = "ULTRA_SECRET_SWARM_PAYLOAD_42".toByteArray(Charsets.UTF_8)
            assertTrue(swarm.writeShardBlock(0, secretBytes))

            val encryptedFrame = swarm.readEncryptedShardFrame(0)
            assertNotNull("Encrypted frame must be readable", encryptedFrame)
            assertFalse(
                "Encrypted frame must NEVER contain plaintext bytes",
                String(encryptedFrame!!, Charsets.UTF_8).contains("ULTRA_SECRET_SWARM_PAYLOAD_42")
            )

            swarm.startNode()
            Thread.sleep(250)

            // 1. Unauthenticated TCP client sending bad HMAC must receive -2 rejection
            Socket("127.0.0.1", SwarmStorageManager.TCP_STREAM_PORT).use { socket ->
                socket.soTimeout = 3000
                val input = DataInputStream(socket.getInputStream())
                val output = DataOutputStream(socket.getOutputStream())
                val challenge = ByteArray(32)
                input.readFully(challenge)

                output.write(ByteArray(32) { 0x5A.toByte() }) // Forged HMAC
                output.writeInt(0)
                output.flush()

                val responseCode = input.readInt()
                assertEquals("Unauthenticated peer must be rejected with -2", -2, responseCode)
            }

            // 2. Authenticated cluster peer receives exact verified ciphertext frame
            val fetchedFrame = swarm.fetchShardFromPeer("127.0.0.1", 0)
            assertNotNull("Authenticated peer must receive encrypted frame", fetchedFrame)
            assertArrayEquals(encryptedFrame, fetchedFrame)
        } finally {
            swarm.stopNode()
            swarm.deallocateContainer()
        }
    }

    @Test
    fun c5_migration15To16_preservesUserProfileAndAddsCloudRewardClaimedColumn() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val dbName = "test_migration_15_16_${System.currentTimeMillis()}.db"
        app.deleteDatabase(dbName)

        try {
            // Create v15 user_profile table without cloudRewardClaimed
            val v15Helper = FrameworkSQLiteOpenHelperFactory().create(
                SupportSQLiteOpenHelper.Configuration.builder(app)
                    .name(dbName)
                    .callback(object : SupportSQLiteOpenHelper.Callback(15) {
                        override fun onCreate(db: SupportSQLiteDatabase) {
                            db.execSQL(
                                """
                                CREATE TABLE IF NOT EXISTS user_profile (
                                    id INTEGER NOT NULL PRIMARY KEY,
                                    username TEXT NOT NULL,
                                    weed INTEGER NOT NULL,
                                    totalDraws INTEGER NOT NULL,
                                    totalMints INTEGER NOT NULL,
                                    totalTrades INTEGER NOT NULL,
                                    isSecured INTEGER NOT NULL,
                                    securedDob TEXT NOT NULL,
                                    securedFirst TEXT NOT NULL,
                                    securedLast TEXT NOT NULL,
                                    seedPhrase TEXT NOT NULL,
                                    pinCode TEXT NOT NULL,
                                    pinSafeMode TEXT,
                                    pinPanicMode TEXT,
                                    proxyType TEXT NOT NULL,
                                    proxyHost TEXT NOT NULL,
                                    proxyPort INTEGER NOT NULL,
                                    proxyConfig TEXT NOT NULL,
                                    isProxyActive INTEGER NOT NULL,
                                    systemLanguage TEXT NOT NULL,
                                    isCloudAllocated INTEGER NOT NULL,
                                    allocatedDiskSpaceGb REAL NOT NULL,
                                    balanceEth REAL NOT NULL,
                                    balanceTon REAL NOT NULL,
                                    balanceSol REAL NOT NULL,
                                    balanceTrx REAL NOT NULL,
                                    balanceUsdc REAL NOT NULL,
                                    completedQuestsCsv TEXT NOT NULL,
                                    nodeStake INTEGER NOT NULL,
                                    nodeUptimeMinutes INTEGER NOT NULL,
                                    nodeLastActiveTime INTEGER NOT NULL,
                                    nodeBandwidthMbps REAL NOT NULL,
                                    nodeIsOnline INTEGER NOT NULL,
                                    alias TEXT NOT NULL,
                                    avatarIndex INTEGER NOT NULL,
                                    bio TEXT NOT NULL,
                                    customAvatarUri TEXT
                                )
                                """.trimIndent()
                            )
                            db.execSQL(
                                """
                                INSERT INTO user_profile VALUES (
                                    1, 'LegacyRunner', 777, 1, 2, 3,
                                    1, '1990-01-01', 'John', 'Doe', 'seed', '12345678', NULL, NULL,
                                    'SOCKS', '127.0.0.1', 9050, 'cfg', 0, 'en',
                                    0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                                    '', 0, 0, 0, 0.0, 0,
                                    'LegacyRunner', 0, '', NULL
                                )
                                """.trimIndent()
                            )
                        }

                        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {}
                    })
                    .build()
            )
            v15Helper.writableDatabase
            v15Helper.close()

            // Re-open at v16 running TarotDatabase.MIGRATION_15_16
            val v16Helper = FrameworkSQLiteOpenHelperFactory().create(
                SupportSQLiteOpenHelper.Configuration.builder(app)
                    .name(dbName)
                    .callback(object : SupportSQLiteOpenHelper.Callback(16) {
                        override fun onCreate(db: SupportSQLiteDatabase) {}
                        override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) {
                            TarotDatabase.MIGRATION_15_16.migrate(db)
                        }
                    })
                    .build()
            )
            val db16 = v16Helper.writableDatabase
            db16.query("SELECT username, weed, cloudRewardClaimed FROM user_profile WHERE id = 1").use { cursor ->
                assertTrue("Migrated row must exist", cursor.moveToFirst())
                assertEquals("LegacyRunner", cursor.getString(0))
                assertEquals(777, cursor.getInt(1))
                assertEquals("cloudRewardClaimed must default to 0", 0, cursor.getInt(2))
            }
            v16Helper.close()
        } finally {
            app.deleteDatabase(dbName)
        }
    }

    @Test
    fun h3_updateCloudAllocation_isIdempotent_awardsBonusOnlyOnce() = runBlocking {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val repository = TarotRepository.getInstance(app)
        repository.userProfile.first { it != null }

        // Start from a known clean baseline
        repository.triggerPanicResetComplete()
        val baseline = repository.getUserProfileSync()!!
        assertFalse(baseline.cloudRewardClaimed)
        val startWeed = baseline.weed

        val firstGranted = repository.updateCloudAllocation(allocated = true, spaceGb = 4.0f)
        assertTrue("First allocation must award +100 WEED bonus", firstGranted)
        val afterFirst = repository.getUserProfileSync()!!
        assertEquals(startWeed + 100, afterFirst.weed)
        assertTrue(afterFirst.cloudRewardClaimed)

        val deallocated = repository.updateCloudAllocation(allocated = false, spaceGb = 0f)
        assertFalse("Deallocation must not award bonus", deallocated)
        assertEquals(startWeed + 100, repository.getUserProfileSync()!!.weed)

        val secondGranted = repository.updateCloudAllocation(allocated = true, spaceGb = 4.0f)
        assertFalse("Repeated allocation must NOT farm duplicate +100 WEED bonus", secondGranted)
        assertEquals(startWeed + 100, repository.getUserProfileSync()!!.weed)
    }

    @Test
    fun m7_swarmShutdown_stopsNode_unmountsContainer_andRemainsReusable() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val swarm = SwarmStorageManager.getInstance(app)

        try {
            val sizeBytes = 10L * 1024L * 1024L
            var callbackResult: Boolean? = null
            swarm.allocateContainer(sizeBytes, "SwarmShutdown#2026") { ok ->
                callbackResult = ok
            }

            val deadline = System.currentTimeMillis() + 10_000L
            while ((swarm.isAllocating.value || callbackResult == null) && System.currentTimeMillis() < deadline) {
                Shadows.shadowOf(Looper.getMainLooper()).idle()
                Thread.sleep(50)
            }
            assertEquals("Container allocation must succeed", true, callbackResult)
            assertTrue(swarm.mountContainer("SwarmShutdown#2026"))
            swarm.startNode()
            Thread.sleep(150)
            assertTrue(swarm.containerMounted.value)
            assertTrue(swarm.nodeActive.value)

            swarm.shutdown()
            assertFalse("Node must stop on shutdown()", swarm.nodeActive.value)
            assertFalse("Container must unmount on shutdown()", swarm.containerMounted.value)
            assertFalse("Allocation flag must reset on shutdown()", swarm.isAllocating.value)

            // Verify SupervisorJob wasn't killed so singleton remains reusable
            assertTrue("Singleton must remain mountable after shutdown()", swarm.mountContainer("SwarmShutdown#2026"))
            swarm.startNode()
            Thread.sleep(150)
            assertTrue("Singleton must be able to restart node after shutdown()", swarm.nodeActive.value)
        } finally {
            swarm.shutdown()
            swarm.deallocateContainer()
        }
    }
}
