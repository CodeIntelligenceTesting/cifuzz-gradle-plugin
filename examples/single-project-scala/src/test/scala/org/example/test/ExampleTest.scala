package org.example.test

import com.code_intelligence.jazzer.junit.FuzzTest
import org.example.ExampleLib
import org.junit.jupiter.api.Assertions.assertTrue

class ExampleTest {

    @FuzzTest
    def testA(data: Array[Byte]) : Unit =
        assertTrue(new ExampleLib().run())

}