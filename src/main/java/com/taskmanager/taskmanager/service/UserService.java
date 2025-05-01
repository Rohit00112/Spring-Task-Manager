package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.repository.UserRepository;
import com.taskmanager.taskmanager.utils.PasswordUtil;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Optional;


@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User saveUser(User user) {
        user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
        return userRepository.save(user);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }
}
