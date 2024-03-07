package org.example.springdemo.junit5features;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;

public class TestDurationExtension implements BeforeTestExecutionCallback, AfterTestExecutionCallback {
    private static final String START_TIME = "start_time";

    @Override
    public void beforeTestExecution(ExtensionContext context) {
        context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()))
                .put(START_TIME, System.currentTimeMillis());
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        long startTime = context.getStore(ExtensionContext.Namespace.create(getClass(), context.getRequiredTestMethod()))
                .remove(START_TIME, long.class);
        long duration = System.currentTimeMillis() - startTime;
        System.out.printf("Test '%s' took %d ms%n", context.getDisplayName(), duration);
    }
}
