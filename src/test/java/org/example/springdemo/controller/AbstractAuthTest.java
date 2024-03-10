package org.example.springdemo.controller;

import org.junit.jupiter.params.provider.Arguments;

import java.util.stream.Stream;

import static org.junit.jupiter.params.provider.Arguments.of;

public class AbstractAuthTest {
    protected static final String AUTHENTICATION_IS_REQUIRED = "Full authentication is required to access this resource";

    private static final String USER_ROLE = "USER";
    private static final String MODERATOR_ROLE = "MODERATOR";
    private static final String ADMIN_ROLE = "ADMIN";
    private static final String USER_NAME = "user";
    private static final String MODERATOR_NAME = "moderator";
    private static final String ADMIN_NAME = "admin";

    protected static Stream<Arguments> withMockModeratorAdmin() {
        return Stream.of(
                of(MODERATOR_NAME, new String[]{USER_ROLE, MODERATOR_ROLE}),
                of(ADMIN_NAME, new String[]{USER_ROLE, MODERATOR_ROLE, ADMIN_ROLE})
        );
    }

    protected static Stream<Arguments> withMockUserModerator() {
        return Stream.of(
                of(USER_NAME, new String[]{USER_ROLE}),
                of(MODERATOR_NAME, new String[]{USER_ROLE, MODERATOR_ROLE})
        );
    }
}
