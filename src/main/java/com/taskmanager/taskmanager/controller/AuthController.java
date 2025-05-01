package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.AuthRequest;
import com.taskmanager.taskmanager.service.UserService;
import com.taskmanager.taskmanager.utils.PasswordUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.taskmanager.taskmanager.model.User;

import java.util.Map;
import java.util.Optional;

@RestController
class AuthController {

    @Autowired
    private UserService userService;

    @PostMapping("/auth/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest authRequest) {
        User user = new User();
        user.setUsername(authRequest.getUsername());
        user.setPassword(authRequest.getPassword());
        user.setEmail(authRequest.getEmail());

        if (userService.findUserByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }

        if (userService.findUserByUsername(user.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }

       User savedUser = userService.saveUser(user);
        return ResponseEntity.ok(savedUser);
    }

    @PostMapping("/auth/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        Optional<User> user = userService.findUserByUsername(authRequest.getEmail());
        assert user.isPresent();
        boolean checkPassword = PasswordUtil.checkPassword(authRequest.getPassword(), user.get().getPassword());
        if (!checkPassword) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid username or password"));
        }

        return ResponseEntity.ok("Logged in successfully");
    }
}
