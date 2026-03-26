package com.origin.exercise.urlshortener.service

import com.origin.exercise.urlshortener.model.ShortenedUrl
import com.origin.exercise.urlshortener.repository.UrlRepository
import com.origin.exercise.urlshortener.validation.ValidUrl
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.validation.annotation.Validated

@Service
@Validated
class UrlShortenerService(
    private val repository: UrlRepository,
    private val codeGenerator: CodeGenerator,
    @Value("\${url-shortener.max-retries:25}") private val maxRetries: Int
) {

    fun shorten(@ValidUrl originalUrl: String): ShortenedUrl {
        return saveWithUniqueCode(originalUrl)
    }

    fun resolve(code: String): ShortenedUrl {
        return repository.findByCode(code)
            ?: throw UrlNotFoundException("No URL found for code: $code")
    }

    private fun saveWithUniqueCode(originalUrl: String): ShortenedUrl {
        repeat(maxRetries) {
            val code = codeGenerator.generate()
            val saved = repository.saveIfAbsent(ShortenedUrl(code = code, originalUrl = originalUrl))
            if (saved != null) return saved
        }
        throw CodeCollisionException("Failed to generate a unique code after $maxRetries attempts")
    }
}

class UrlNotFoundException(message: String) : RuntimeException(message)
class CodeCollisionException(message: String) : RuntimeException(message)
