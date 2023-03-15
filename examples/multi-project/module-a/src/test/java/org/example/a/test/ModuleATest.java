package org.example.a.test;

import com.code_intelligence.jazzer.junit.FuzzTest;
import org.example.a.ModuleA;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModuleATest {

    @FuzzTest
    public void testA(byte[] data) {
        assertTrue(new ModuleA().run());
    }
}
