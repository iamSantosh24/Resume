package com.example.resume

import okhttp3.OkHttpClient
import okhttp3.Request
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class BackendSmokeTest {
    private val client = OkHttpClient()

    @Test
    fun testResumeEndpointIsReachable() {
        val req = Request.Builder()
            .url("http://localhost:4000/resume")
            .get()
            .build()

        client.newCall(req).execute().use { resp ->
            // assert status code
            assertEquals("Expected HTTP 200 from /resume", 200, resp.code)

            val body = resp.body?.string() ?: ""
            assertTrue("Response body should be non-empty JSON", body.trim().isNotEmpty())

            // optional: basic sanity check that it's JSON-like
            assertTrue("Response should start with '{' or '['", body.trim().startsWith("{") || body.trim().startsWith("["))
        }
    }
}

