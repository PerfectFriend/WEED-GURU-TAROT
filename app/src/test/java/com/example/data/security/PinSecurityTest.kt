package com.example.data.security

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.TarotRepository
import com.example.data.UserProfile
import com.example.data.crypto.CryptoHelper
import com.example.data.crypto.SecureVaultHelper
import com.example.viewmodel.TarotViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class PinSecurityTest {

    private lateinit var app: Application
    private lateinit var repository: TarotRepository
    private lateinit var viewModel: TarotViewModel

    @Before
    fun setup() {
        app = ApplicationProvider.getApplicationContext()
        SecureVaultHelper.resetPinLockout(app)
        repository = TarotRepository.getInstance(app)
        viewModel = TarotViewModel(app)
    }

    @Test
    fun userProfile_defaults_haveNoHardcodedPinBackdoors() {
        val profile = UserProfile()
        // Must NOT default to "11111111" or "22222222"
        assertNull("pinSafeMode must default to null", profile.pinSafeMode)
        assertNull("pinPanicMode must default to null", profile.pinPanicMode)
        assertEquals("pinCode must default to empty", "", profile.pinCode)
    }

    @Test
    fun panicPin_doesNotWipeDatabase_andFailsUnlock() = runBlocking {
        repository.userProfile.first { it != null }

        // Save a secured profile with PIN 87654321
        repository.saveSecuredProfile(
            dob = "1990-01-01",
            first = "Alice",
            last = "Cyber",
            seed = "test seed phrase not real entropy",
            pin = "87654321"
        )

        val profile = repository.getUserProfileSync()
        assertNotNull(profile)
        assertNotEquals("PIN must not be stored in plaintext", "87654321", profile!!.pinCode)
        assertTrue("PIN must be stored as PBKDF2_V1 hash", CryptoHelper.isPinHashed(profile.pinCode))
        assertTrue("PBKDF2 PIN verification must succeed for valid PIN", CryptoHelper.verifyPin("87654321", profile.pinCode))
        assertTrue("Seed phrase must be Keystore-sealed", SecureVaultHelper.isEncrypted(profile.seedPhrase))
        assertEquals("test seed phrase not real entropy", repository.getDecryptedSeedPhrase())
        assertEquals("PII DOB must be scrubbed", "", profile.securedDob)
        assertEquals("PII First Name must be scrubbed", "", profile.securedFirst)
        assertEquals("PII Last Name must be scrubbed", "", profile.securedLast)
        assertNull(profile.pinPanicMode)

        // Attempt to enter old backdoor "22222222"
        val unlockResult = viewModel.unlockApp("22222222")
        assertFalse("Old backdoor '22222222' must NOT unlock app", unlockResult)

        // Verify database is NOT wiped
        val profileAfter = repository.getUserProfileSync()
        assertEquals("Profile must NOT be wiped by entering 22222222", profile.pinCode, profileAfter?.pinCode)
        assertEquals("test seed phrase not real entropy", repository.getDecryptedSeedPhrase())
    }

    @Test
    fun rateLimiting_locksOutAfter5FailedAttempts_andPersistsAcrossRestart() = runBlocking {
        repository.userProfile.first { it != null }
        SecureVaultHelper.resetPinLockout(app)

        repository.saveSecuredProfile(
            dob = "1990-01-01",
            first = "Bob",
            last = "Netrunner",
            seed = "test seed",
            pin = "12345678"
        )

        // 4 failed attempts
        for (i in 1..4) {
            val result = viewModel.unlockApp("00000000")
            assertFalse(result)
            assertEquals(i, viewModel.pinFailedAttempts.value)
        }

        // 5th failed attempt should trigger persistent lockout
        val fifthResult = viewModel.unlockApp("00000000")
        assertFalse(fifthResult)
        assertEquals(5, viewModel.pinFailedAttempts.value)
        assertTrue("Lockout timestamp must be set in the future", viewModel.lockoutUntilMs.value > System.currentTimeMillis())

        // Even with the correct PIN, during lockout access must be denied
        val attemptDuringLockout = viewModel.unlockApp("12345678")
        assertFalse("Must reject unlock attempts while rate-limited lockout is active", attemptDuringLockout)

        // Recreate ViewModel (simulating app kill/restart) -> lockout must STILL hold (H4)
        val freshViewModel = TarotViewModel(app)
        assertEquals(5, freshViewModel.pinFailedAttempts.value)
        assertTrue(freshViewModel.lockoutUntilMs.value > System.currentTimeMillis())
        assertFalse("Lockout must survive app/ViewModel restart", freshViewModel.unlockApp("12345678"))
    }
}

