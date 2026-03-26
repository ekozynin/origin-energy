package com.origin.exercise.urlshortener.service

import com.origin.exercise.urlshortener.repository.UrlRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class UrlShortenerServiceTest {

    private val repository = UrlRepository()
    private val defaultAlphabet = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    private val codeGenerator = CodeGenerator("", 6, defaultAlphabet)
    private val service = UrlShortenerService(repository, codeGenerator, 25)

    @Test
    fun `shorten creates a shortened URL with a unique code`() {
        val result = service.shorten("https://www.originenergy.com.au/electricity-gas/plans.html")

        assertEquals("https://www.originenergy.com.au/electricity-gas/plans.html", result.originalUrl)
        assertEquals(6, result.code.length)
        assertNotNull(result.createdAt)
    }

    @Test
    fun `shorten generates different codes for different URLs`() {
        val result1 = service.shorten("https://example.com/page1")
        val result2 = service.shorten("https://example.com/page2")

        assertNotEquals(result1.code, result2.code)
    }

    @Test
    fun `resolve returns the original URL for a valid code`() {
        val shortened = service.shorten("https://example.com")
        val resolved = service.resolve(shortened.code)

        assertEquals("https://example.com", resolved.originalUrl)
    }

    @Test
    fun `resolve throws UrlNotFoundException for unknown code`() {
        assertThrows<UrlNotFoundException> {
            service.resolve("nonexistent")
        }
    }

    @Test
    fun `resolve error message contains the code`() {
        val exception = assertThrows<UrlNotFoundException> {
            service.resolve("xyz789")
        }
        assertTrue(exception.message!!.contains("xyz789"))
    }

    @Test
    fun `shorten accepts http URL`() {
        val result = service.shorten("http://example.com")
        assertEquals("http://example.com", result.originalUrl)
    }

    @Test
    fun `shorten accepts https URL with path and query`() {
        val url = "https://example.com/path?query=value&other=123"
        val result = service.shorten(url)
        assertEquals(url, result.originalUrl)
    }

    @Test
    fun `shorten accepts URL with fragment`() {
        val url = "https://example.com/page#section"
        val result = service.shorten(url)
        assertEquals(url, result.originalUrl)
    }

    @Test
    fun `shorten throws CodeCollisionException when max retries exhausted`() {
        val repo = UrlRepository()
        val fixedCode = "aaaaaa"
        repo.saveIfAbsent(com.origin.exercise.urlshortener.model.ShortenedUrl(code = fixedCode, originalUrl = "https://taken.com"))

        val alwaysSameCode = object : CodeGenerator("", 6, defaultAlphabet) {
            override fun generate(): String = fixedCode
        }
        val serviceWithCollisions = UrlShortenerService(repo, alwaysSameCode, 3)

        assertThrows<CodeCollisionException> {
            serviceWithCollisions.shorten("https://example.com")
        }
    }

    @Test
    fun `shorten retries on collision and succeeds`() {
        val repo = UrlRepository()
        val collidingCode = "aaaaaa"
        repo.saveIfAbsent(com.origin.exercise.urlshortener.model.ShortenedUrl(code = collidingCode, originalUrl = "https://taken.com"))

        var callCount = 0
        val generatorWithOneCollision = object : CodeGenerator("", 6, defaultAlphabet) {
            override fun generate(): String {
                callCount++
                return if (callCount == 1) collidingCode else "bbbbbb"
            }
        }
        val serviceWithRetry = UrlShortenerService(repo, generatorWithOneCollision, 5)

        val result = serviceWithRetry.shorten("https://example.com")
        assertEquals("bbbbbb", result.code)
        assertEquals(2, callCount)
    }
}
