package com.example.resume

import org.junit.Test
import org.junit.Assert.*

class ResumeParserTest {
    @Test
    fun photoKeys_areRecognized() {
        val keys = listOf("photoUrl", "photo_url", "photo", "avatarUrl", "avatar_url", "avatar")
        keys.forEach { key ->
            val personal = mapOf(key to "https://example.com/pic.jpg", "name" to "Test")
            val raw = mapOf("personalInfo" to personal)
            val parsed = parseResumeFromMap(raw)
            assertNotNull("personalInfo should not be null for key=$key", parsed.personalInfo)
            assertEquals("https://example.com/pic.jpg", parsed.personalInfo?.profilePicture)
        }
    }

    @Test
    fun missingPhoto_returnsNull() {
        val raw = mapOf("personalInfo" to mapOf("name" to "Test"))
        val parsed = parseResumeFromMap(raw)
        assertNotNull(parsed.personalInfo)
        assertNull(parsed.personalInfo?.profilePicture)
    }
}

