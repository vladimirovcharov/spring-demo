package org.example.springdemo.junit5features;

import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

public class LoggingExtension implements BeforeEachCallback, TestInstancePostProcessor {
    @Override
    public void beforeEach(ExtensionContext context) {
        System.out.println("Starting test: " + context.getDisplayName());
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) {
        System.out.println("Test instance processed for: " + context.getDisplayName());
    }
}
