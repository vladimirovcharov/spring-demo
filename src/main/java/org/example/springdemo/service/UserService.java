package org.example.springdemo.service;

import org.example.springdemo.model.User;
import org.example.springdemo.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {
    final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(long id) {
        return userRepository.findById(id).orElse(null);
    }

    public User save(User user) {
        User save = userRepository.save(user);
        System.out.println(save);
        return save;
    }

    public void deleteById(long id) {
        userRepository.deleteById(id);
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }
}
