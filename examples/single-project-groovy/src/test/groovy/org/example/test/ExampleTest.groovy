package org.example.test

import com.code_intelligence.jazzer.junit.FuzzTest
import org.example.ExampleLib

import static org.junit.jupiter.api.Assertions.assertTrue

class ExampleTest {

    @FuzzTest
    void 'a groovy fuzz test'(byte[] data) {
        assertTrue new ExampleLib().run()
    }
}
