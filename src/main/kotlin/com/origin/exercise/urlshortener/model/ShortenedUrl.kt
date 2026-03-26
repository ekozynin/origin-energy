package com.origin.exercise.urlshortener.model

import java.time.Instant

data class ShortenedUrl(
    val code: String,
    val originalUrl: String,
    val createdAt: Instant = Instant.now()
)
