package org.example.c.test;

import com.code_intelligence.jazzer.junit.FuzzTest;
import org.example.a.ModuleA;
import org.example.b.ModuleB;
import org.example.c.ModuleC;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModuleCTest {

    @FuzzTest
    public void testC(byte[] data) {
        // assertTrue(new ModuleA().run());
        assertTrue(new ModuleB().run());
        assertTrue(new ModuleC().run());
    }
}
