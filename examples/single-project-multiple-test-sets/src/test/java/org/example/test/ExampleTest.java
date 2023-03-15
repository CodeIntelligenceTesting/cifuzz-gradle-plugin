package org.example.test;

import com.code_intelligence.jazzer.junit.FuzzTest;
import org.example.ExampleLib;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExampleTest {

    @FuzzTest
    public void testA(byte[] data) {
        assertTrue(new ExampleLib().run());
    }
}
