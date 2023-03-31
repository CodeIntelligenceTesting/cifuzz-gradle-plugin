package org.example.test

import com.code_intelligence.jazzer.junit.FuzzTest
import org.example.ExampleLib

import org.junit.jupiter.api.Assertions.assertTrue

class ExampleTest {

    @FuzzTest
    fun fuzzTest(data: ByteArray) {
        assertTrue(ExampleLib().run());
    }
}
