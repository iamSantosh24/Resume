package com.example.resume

import org.junit.Test
import org.junit.Assert.*

class ContactLabelUtilsTest {

    // --- GitHub tests ---
    @Test
    fun github_fullUrl_profile() {
        val input = "github.com/janedoe"
        val expected = "@janedoe"
        assertEquals(expected, displayGithubLabel(input))
    }

    @Test
    fun github_https_userRepo() {
        val input = "https://github.com/user/repo"
        val expected = "user/repo"
        assertEquals(expected, displayGithubLabel(input))
    }

    @Test
    fun github_bareUsername() {
        val input = "janedoe"
        val expected = "@janedoe"
        assertEquals(expected, displayGithubLabel(input))
    }

    @Test
    fun github_withAtPrefix() {
        val input = "@janedoe"
        val expected = "@janedoe"
        assertEquals(expected, displayGithubLabel(input))
    }

    @Test
    fun github_www_prefix() {
        val input = "www.github.com/janedoe"
        val expected = "@janedoe"
        assertEquals(expected, displayGithubLabel(input))
    }

    @Test
    fun github_trailingSlashOnly_returnsOriginal() {
        val input = "github.com/"
        val expected = input
        assertEquals(expected, displayGithubLabel(input))
    }

    @Test
    fun github_gist_likePath_keepsRepoPath() {
        val input = "https://gist.github.com/janedoe/12345"
        val expected = "janedoe/12345"
        assertEquals(expected, displayGithubLabel(input))
    }

    // --- LinkedIn tests ---
    @Test
    fun linkedin_fullUrl_inProfile() {
        val input = "linkedin.com/in/janedoe"
        val expected = "in/janedoe"
        assertEquals(expected, displayLinkedInLabel(input))
    }

    @Test
    fun linkedin_shorthand_inProfile() {
        val input = "in/janedoe"
        val expected = "in/janedoe"
        assertEquals(expected, displayLinkedInLabel(input))
    }

    @Test
    fun linkedin_leadingSlash_inProfile() {
        val input = "/in/janedoe"
        val expected = "in/janedoe"
        assertEquals(expected, displayLinkedInLabel(input))
    }

    @Test
    fun linkedin_bareUsername_mapsToIn() {
        val input = "janedoe"
        val expected = "in/janedoe"
        assertEquals(expected, displayLinkedInLabel(input))
    }

    @Test
    fun linkedin_atPrefix_mapsToIn() {
        val input = "@janedoe"
        val expected = "in/janedoe"
        assertEquals(expected, displayLinkedInLabel(input))
    }

    @Test
    fun linkedin_companyPath_keepsTransformed() {
        val input = "www.linkedin.com/company/acme"
        val expected = "in/company/acme" // current heuristic prefixes with 'in/' for non-empty non-in tokens
        assertEquals(expected, displayLinkedInLabel(input))
    }

    @Test
    fun linkedin_trailingSlashOnly_returnsOriginal() {
        val input = "linkedin.com/"
        val expected = input
        assertEquals(expected, displayLinkedInLabel(input))
    }
}

