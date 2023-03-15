package org.example.test;

import org.example.ExampleLib;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class ExampleTest {

    @Test
    public void testA() {
        assertTrue(new ExampleLib().run());
    }
}
