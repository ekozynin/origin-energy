package com.origin.exercise.urlshortener.controller

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class UrlShortenerControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @Test
    fun `POST shorten returns 201 with short URL`() {
        mockMvc.perform(
            post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"url": "https://www.originenergy.com.au/electricity-gas/plans.html"}""")
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.shortUrl").isNotEmpty)
            .andExpect(jsonPath("$.originalUrl").value("https://www.originenergy.com.au/electricity-gas/plans.html"))
    }

    @Test
    fun `POST shorten returns 400 for invalid URL`() {
        mockMvc.perform(
            post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"url": "not-a-valid-url"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").isNotEmpty)
    }

    @Test
    fun `POST shorten returns 400 for empty body`() {
        mockMvc.perform(
            post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `GET redirect returns 302 for existing code`() {
        val response = mockMvc.perform(
            post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"url": "https://example.com"}""")
        ).andReturn().response

        val shortUrl = objectMapper.readTree(response.contentAsString)["shortUrl"].asText()
        val code = shortUrl.substringAfterLast("/")

        mockMvc.perform(get("/$code"))
            .andExpect(status().isFound)
            .andExpect(header().string("Location", "https://example.com"))
    }

    @Test
    fun `GET redirect returns 404 for unknown code`() {
        mockMvc.perform(get("/unknown123"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").isNotEmpty)
    }

    @Test
    fun `GET info returns URL details for existing code`() {
        val response = mockMvc.perform(
            post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"url": "https://example.com/info-test"}""")
        ).andReturn().response

        val shortUrl = objectMapper.readTree(response.contentAsString)["shortUrl"].asText()
        val code = shortUrl.substringAfterLast("/")

        mockMvc.perform(get("/$code/info"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.code").value(code))
            .andExpect(jsonPath("$.originalUrl").value("https://example.com/info-test"))
            .andExpect(jsonPath("$.createdAt").isNotEmpty)
    }

    @Test
    fun `GET info returns 404 for unknown code`() {
        mockMvc.perform(get("/unknown123/info"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.error").isNotEmpty)
    }

    @Test
    fun `POST shorten returns 400 for malformed JSON`() {
        mockMvc.perform(
            post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("not json")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("Invalid request body"))
    }

    @Test
    fun `POST shorten returns 400 for null URL field`() {
        mockMvc.perform(
            post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"url": null}""")
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `POST shorten returns 400 for ftp scheme`() {
        mockMvc.perform(
            post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"url": "ftp://files.example.com/data"}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("URL must be a valid http or https URL"))
    }

    @Test
    fun `POST shorten returns 400 for whitespace-only URL`() {
        mockMvc.perform(
            post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"url": "   "}""")
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.error").value("URL must be a valid http or https URL"))
    }

    @Test
    fun `POST shorten preserves URL with query and fragment`() {
        val url = "https://example.com/path?q=1&r=2#section"
        val response = mockMvc.perform(
            post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""{"url": "$url"}""")
        )
            .andExpect(status().isCreated)
            .andReturn().response

        val originalUrl = objectMapper.readTree(response.contentAsString)["originalUrl"].asText()
        assertEquals(url, originalUrl)
    }
}
