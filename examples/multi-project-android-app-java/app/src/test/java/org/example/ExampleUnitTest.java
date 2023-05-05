package org.example;

import com.code_intelligence.jazzer.junit.FuzzTest;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExampleUnitTest {

    @FuzzTest
    void fuzzTest(byte[] data) {
        assertTrue(new MainFeature().doSomething());
    }

    @Test
    void unitTest() {
        assertEquals(4, 2 + 2);
    }
}