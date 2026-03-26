package com.origin.exercise.urlshortener.repository

import com.origin.exercise.urlshortener.model.ShortenedUrl
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UrlRepositoryTest {

    private val repository = UrlRepository()

    @Test
    fun `saveIfAbsent stores and returns the shortened URL`() {
        val url = ShortenedUrl(code = "abc123", originalUrl = "https://example.com")
        val result = repository.saveIfAbsent(url)

        assertEquals(url, result)
    }

    @Test
    fun `saveIfAbsent returns null when code already exists`() {
        repository.saveIfAbsent(ShortenedUrl(code = "abc123", originalUrl = "https://first.com"))
        val result = repository.saveIfAbsent(ShortenedUrl(code = "abc123", originalUrl = "https://second.com"))

        assertNull(result)
    }

    @Test
    fun `saveIfAbsent does not overwrite existing entry`() {
        repository.saveIfAbsent(ShortenedUrl(code = "abc123", originalUrl = "https://first.com"))
        repository.saveIfAbsent(ShortenedUrl(code = "abc123", originalUrl = "https://second.com"))

        val result = repository.findByCode("abc123")
        assertEquals("https://first.com", result?.originalUrl)
    }

    @Test
    fun `findByCode returns saved URL`() {
        val url = ShortenedUrl(code = "abc123", originalUrl = "https://example.com")
        repository.saveIfAbsent(url)

        val result = repository.findByCode("abc123")

        assertEquals(url, result)
    }

    @Test
    fun `findByCode returns null for unknown code`() {
        assertNull(repository.findByCode("nonexistent"))
    }
}
