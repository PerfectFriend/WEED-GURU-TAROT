package com.example.data

import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.concurrent.atomic.AtomicInteger

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GeminiServiceTest {

    @After
    fun resetModel() {
        GeminiService.configureModel("gemini-2.5-flash")
    }

    @Test
    fun configureModel_updatesEndpointAndIgnoresBlank() {
        GeminiService.configureModel("  gemini-2.5-pro  ")
        assertEquals(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent",
            GeminiService.buildEndpointUrl()
        )

        GeminiService.configureModel("   ")
        assertEquals(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-pro:generateContent",
            GeminiService.buildEndpointUrl()
        )
    }

    @Test
    fun executeGeminiCall_retriesTransient429And503_thenSucceeds() = runBlocking {
        val attempts = AtomicInteger(0)
        val mockClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val attempt = attempts.incrementAndGet()
                val req = chain.request()
                when (attempt) {
                    1 -> Response.Builder()
                        .request(req)
                        .protocol(Protocol.HTTP_1_1)
                        .code(429)
                        .message("Too Many Requests")
                        .body("{}".toResponseBody("application/json".toMediaType()))
                        .build()
                    2 -> Response.Builder()
                        .request(req)
                        .protocol(Protocol.HTTP_1_1)
                        .code(503)
                        .message("Service Unavailable")
                        .body("{}".toResponseBody("application/json".toMediaType()))
                        .build()
                    else -> Response.Builder()
                        .request(req)
                        .protocol(Protocol.HTTP_1_1)
                        .code(200)
                        .message("OK")
                        .body(
                            """{"candidates":[{"content":{"parts":[{"text":"NODE_OK"}]}}]}"""
                                .toResponseBody("application/json".toMediaType())
                        )
                        .build()
                }
            }
            .build()

        val result = GeminiService.executeGeminiCall(
            apiKey = "test-key",
            userPrompt = "ping",
            httpClient = mockClient
        )

        assertEquals(3, attempts.get())
        assertEquals("NODE_OK", result)
    }

    @Test
    fun executeGeminiCall_doesNotRetryNonTransient400() = runBlocking {
        val attempts = AtomicInteger(0)
        val mockClient = OkHttpClient.Builder()
            .addInterceptor { chain ->
                attempts.incrementAndGet()
                Response.Builder()
                    .request(chain.request())
                    .protocol(Protocol.HTTP_1_1)
                    .code(400)
                    .message("Bad Request")
                    .body("{}".toResponseBody("application/json".toMediaType()))
                    .build()
            }
            .build()

        val result = GeminiService.executeGeminiCall(
            apiKey = "test-key",
            userPrompt = "ping",
            httpClient = mockClient
        )

        assertEquals(1, attempts.get())
        assertNull(result)
    }
}
