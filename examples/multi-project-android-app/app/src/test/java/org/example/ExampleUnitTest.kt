package org.example

import com.code_intelligence.jazzer.junit.FuzzTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class ExampleUnitTest {

    @FuzzTest
    fun fuzzTest(data: ByteArray) {
        assertTrue(MainFeature().doSomething())
    }

    @Test
    fun unitTest() {
        assertEquals(4, 2 + 2)
    }
}