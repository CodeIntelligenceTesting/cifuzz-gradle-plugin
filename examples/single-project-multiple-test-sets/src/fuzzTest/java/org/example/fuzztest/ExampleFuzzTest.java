package org.example.fuzztest;

import com.code_intelligence.jazzer.junit.FuzzTest;
import org.example.ExampleApp;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExampleFuzzTest {

    @FuzzTest
    public void testB(byte[] data) {
        assertTrue(new ExampleApp().run());
    }
}
