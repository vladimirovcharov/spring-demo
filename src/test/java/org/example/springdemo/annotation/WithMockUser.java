package org.example.springdemo.annotation;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@org.springframework.security.test.context.support.WithMockUser
public @interface WithMockUser {
}
