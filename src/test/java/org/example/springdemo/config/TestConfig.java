//package org.example.springdemo.config;
//
//import org.example.springdemo.repository.UserRepository;
//import org.example.springdemo.service.UserService;
//import org.mockito.Mockito;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class TestConfig {
//    @Bean
//    public UserService userService(UserRepository userRepository) {
//        return new UserService(userRepository);
//    }
//
//    @Bean
//    public UserRepository userRepository() {
//        return Mockito.mock(UserRepository.class);
//    }
//}
