package com.origin.exercise.urlshortener.repository

import com.origin.exercise.urlshortener.model.ShortenedUrl
import org.springframework.stereotype.Repository
import java.util.concurrent.ConcurrentHashMap

@Repository
class UrlRepository {

    private val store = ConcurrentHashMap<String, ShortenedUrl>()

    fun saveIfAbsent(shortenedUrl: ShortenedUrl): ShortenedUrl? {
        val existing = store.putIfAbsent(shortenedUrl.code, shortenedUrl)
        return if (existing == null) shortenedUrl else null
    }

    fun findByCode(code: String): ShortenedUrl? = store[code]
}
