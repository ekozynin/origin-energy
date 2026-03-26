package com.origin.exercise.urlshortener.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class CodeGenerator(
    @Value("\${url-shortener.instance-prefix:}") private val instancePrefix: String,
    @Value("\${url-shortener.code-length:6}") private val codeLength: Int,
    @Value("\${url-shortener.alphabet:abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789}") private val alphabet: String
) {

    open fun generate(): String {
        val randomPart = (1..codeLength)
            .map { alphabet.random() }
            .joinToString("")
        return "$instancePrefix$randomPart"
    }
}
