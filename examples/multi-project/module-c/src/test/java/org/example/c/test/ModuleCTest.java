package org.example.c.test;

import com.code_intelligence.jazzer.junit.FuzzTest;
import org.example.a.ModuleA;
import org.example.b.ModuleB;
import org.example.c.ModuleC;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModuleCTest {

    @FuzzTest
    public void fuzzTest(byte[] data) {
        assertTrue(new ModuleA().run());
        assertTrue(new ModuleC().run());
    }

    @Test
    public void unitTest() {
        assertTrue(new ModuleB().run());
    }
}
