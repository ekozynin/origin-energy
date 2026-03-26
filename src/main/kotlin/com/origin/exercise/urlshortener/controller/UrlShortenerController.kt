package com.origin.exercise.urlshortener.controller

import com.origin.exercise.urlshortener.api.UrlShortenerApi
import com.origin.exercise.urlshortener.api.model.ShortenRequest
import com.origin.exercise.urlshortener.api.model.ShortenResponse
import com.origin.exercise.urlshortener.api.model.UrlInfoResponse
import com.origin.exercise.urlshortener.service.UrlShortenerService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI
import java.time.ZoneOffset

@RestController
class UrlShortenerController(private val service: UrlShortenerService) : UrlShortenerApi {

    override fun shortenUrl(shortenRequest: ShortenRequest): ResponseEntity<ShortenResponse> {
        val shortened = service.shorten(shortenRequest.url)
        val shortUrl = buildShortUrl(shortened.code)
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ShortenResponse(shortUrl = shortUrl, originalUrl = shortened.originalUrl))
    }

    override fun redirectToOriginalUrl(code: String): ResponseEntity<Unit> {
        val shortened = service.resolve(code)
        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI(shortened.originalUrl))
            .build()
    }

    override fun getUrlInfo(code: String): ResponseEntity<UrlInfoResponse> {
        val shortened = service.resolve(code)
        return ResponseEntity.ok(
            UrlInfoResponse(
                code = shortened.code,
                originalUrl = shortened.originalUrl,
                createdAt = shortened.createdAt.atOffset(ZoneOffset.UTC)
            )
        )
    }

    private fun buildShortUrl(code: String): String {
        return ServletUriComponentsBuilder.fromCurrentContextPath()
            .path("/{code}")
            .buildAndExpand(code)
            .toUriString()
    }
}
