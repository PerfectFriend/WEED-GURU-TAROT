package com.example.data.security

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.TarotRepository
import com.example.data.UserProfile
import com.example.viewmodel.TarotViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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
    fun panicPin_doesNotWipeDatabase_andFailsUnlock() = runTest {
        // Save a secured profile with PIN 87654321
        repository.saveSecuredProfile(
            dob = "1990-01-01",
            first = "Alice",
            last = "Cyber",
            seed = "test seed phrase not real entropy",
            pin = "87654321"
        )

        // Give state flow a moment to emit
        val profile = repository.getUserProfileSync()
        assertEquals("87654321", profile?.pinCode)
        assertNull(profile?.pinPanicMode)

        // Attempt to enter old backdoor "22222222"
        val unlockResult = viewModel.unlockApp("22222222")
        assertFalse("Old backdoor '22222222' must NOT unlock app", unlockResult)

        // Verify database is NOT wiped
        val profileAfter = repository.getUserProfileSync()
        assertEquals("Profile must NOT be wiped by entering 22222222", "87654321", profileAfter?.pinCode)
    }

    @Test
    fun rateLimiting_locksOutAfter5FailedAttempts() = runTest {
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

        // 5th failed attempt should trigger lockout
        val fifthResult = viewModel.unlockApp("00000000")
        assertFalse(fifthResult)
        assertEquals(5, viewModel.pinFailedAttempts.value)
        assertTrue("Lockout timestamp must be set in the future", viewModel.lockoutUntilMs.value > System.currentTimeMillis())

        // Even with the correct PIN, during lockout access must be denied
        val attemptDuringLockout = viewModel.unlockApp("12345678")
        assertFalse("Must reject unlock attempts while rate-limited lockout is active", attemptDuringLockout)
    }
}
