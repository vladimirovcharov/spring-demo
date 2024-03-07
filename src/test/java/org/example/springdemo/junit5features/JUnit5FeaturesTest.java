package org.example.springdemo.junit5features;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ExtendWith({LoggingExtension.class, TestDurationExtension.class})
public class JUnit5FeaturesTest {
    @TestFactory
    Stream<DynamicTest> dynamicTests() {
        return Stream.of("apple", "mango", "lemon")
                .map(fruit ->
                        DynamicTest.dynamicTest("Length of " + fruit, () -> {
                            int length = fruit.length();
                            assertEquals(5, length);
                        })
                );
    }

    @ParameterizedTest
    @ValueSource(strings = { "apple", "banana", "lemon" })
    @DisplayName("Test length of fruits")
    void testStringLength(String fruit) {
        assertTrue(fruit.length() > 4);
    }

    @Test
    void exampleTest() {
        System.out.println("Executing example test");
    }

    @Test
    void exampleTest2() throws Exception {
        Thread.sleep(1000);
    }

    @Test
    void exampleTest3() throws Exception {
        Thread.sleep(500);
    }
}
