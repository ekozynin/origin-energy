package com.origin.exercise.urlshortener.service

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class CodeGeneratorTest {

    private val defaultAlphabet = "abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789"

    @Test
    fun `generates code of default length 6 with no prefix`() {
        val generator = CodeGenerator("", 6, defaultAlphabet)
        val code = generator.generate()
        assertEquals(6, code.length)
    }

    @Test
    fun `generates non-sequential codes`() {
        val generator = CodeGenerator("", 6, defaultAlphabet)
        val codes = (1..100).map { generator.generate() }.toSet()
        assertTrue(codes.size > 90, "Expected mostly unique codes from 100 generations, got ${codes.size}")
    }

    @Test
    fun `generated code contains only characters from alphabet`() {
        val generator = CodeGenerator("", 6, defaultAlphabet)
        val code = generator.generate()
        assertTrue(code.all { it in defaultAlphabet.toSet() })
    }

    @Test
    fun `prepends instance prefix to generated code`() {
        val generator = CodeGenerator("A1", 6, defaultAlphabet)
        val code = generator.generate()
        assertTrue(code.startsWith("A1"))
        assertEquals(8, code.length)
    }

    @Test
    fun `different prefixes produce non-overlapping codes`() {
        val generatorA = CodeGenerator("AA", 6, defaultAlphabet)
        val generatorB = CodeGenerator("BB", 6, defaultAlphabet)
        val codesA = (1..50).map { generatorA.generate() }.toSet()
        val codesB = (1..50).map { generatorB.generate() }.toSet()
        assertTrue(codesA.intersect(codesB).isEmpty())
    }

    @Test
    fun `respects custom code length`() {
        val generator = CodeGenerator("", 10, defaultAlphabet)
        val code = generator.generate()
        assertEquals(10, code.length)
    }

    @Test
    fun `respects custom alphabet`() {
        val generator = CodeGenerator("", 8, "ABC")
        val code = generator.generate()
        assertEquals(8, code.length)
        assertTrue(code.all { it in setOf('A', 'B', 'C') })
    }

    @Test
    fun `custom alphabet and prefix work together`() {
        val generator = CodeGenerator("ZZ", 4, "XY")
        val code = generator.generate()
        assertEquals(6, code.length)
        assertTrue(code.startsWith("ZZ"))
        assertTrue(code.removePrefix("ZZ").all { it in setOf('X', 'Y') })
    }
}
