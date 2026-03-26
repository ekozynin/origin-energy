package com.origin.exercise.urlshortener.validation

import jakarta.validation.Validation
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UrlValidatorTest {

    private val validator = Validation.buildDefaultValidatorFactory().validator

    data class TestBean(@field:ValidUrl val url: String?)

    private fun isValid(url: String?): Boolean {
        return validator.validate(TestBean(url)).isEmpty()
    }

    @Test
    fun `accepts valid https URL`() {
        assertTrue(isValid("https://example.com"))
    }

    @Test
    fun `accepts valid http URL`() {
        assertTrue(isValid("http://example.com"))
    }

    @Test
    fun `accepts URL with path and query`() {
        assertTrue(isValid("https://example.com/path?q=1&r=2"))
    }

    @Test
    fun `accepts URL with fragment`() {
        assertTrue(isValid("https://example.com/page#section"))
    }

    @Test
    fun `rejects null URL`() {
        assertFalse(isValid(null))
    }

    @Test
    fun `rejects empty URL`() {
        assertFalse(isValid(""))
    }

    @Test
    fun `rejects whitespace-only URL`() {
        assertFalse(isValid("   "))
    }

    @Test
    fun `rejects URL without scheme`() {
        assertFalse(isValid("not-a-url"))
    }

    @Test
    fun `rejects URL with unsupported scheme`() {
        assertFalse(isValid("ftp://example.com/file"))
    }

    @Test
    fun `rejects URL with no host`() {
        assertFalse(isValid("http://"))
    }

    @Test
    fun `rejects mailto scheme`() {
        assertFalse(isValid("mailto:user@example.com"))
    }

    @Test
    fun `rejects file scheme`() {
        assertFalse(isValid("file:///etc/passwd"))
    }

    @Test
    fun `rejects malformed URI that throws exception`() {
        assertFalse(isValid("http://[invalid"))
    }

    @Test
    fun `rejects http URL with null host`() {
        // Opaque URI: scheme present but host is null
        assertFalse(isValid("http:not-a-host"))
    }

    @Test
    fun `rejects https URL with blank host`() {
        assertFalse(isValid("https://"))
    }

    @Test
    fun `accepts http URL with valid host`() {
        assertTrue(isValid("http://example.com/page"))
    }

    @Test
    fun `accepts https URL with valid host`() {
        assertTrue(isValid("https://example.com/page"))
    }
}
