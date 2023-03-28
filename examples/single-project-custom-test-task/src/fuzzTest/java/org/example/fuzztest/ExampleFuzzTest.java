package org.example.fuzztest;

import com.code_intelligence.jazzer.junit.FuzzTest;
import org.example.ExampleApp;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExampleFuzzTest {

    @FuzzTest
    public void fuzzTest(byte[] data) {
        assertTrue(new ExampleApp().run());
    }

    @Test
    public void unitTest() {
        assertTrue(false);
    }
}
