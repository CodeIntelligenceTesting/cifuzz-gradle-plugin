package org.example.a.test;

import org.example.a.ModuleA;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModuleATest {

    @Test
    public void testA(byte[] data) {
        assertTrue(new ModuleA().run());
    }
}
