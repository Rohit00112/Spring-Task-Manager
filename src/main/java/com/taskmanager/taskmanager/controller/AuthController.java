package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.AuthRequest;
import com.taskmanager.taskmanager.dto.AuthResponse; // Added import
import com.taskmanager.taskmanager.security.JwtUtil;
import com.taskmanager.taskmanager.service.UserService;
// import com.taskmanager.taskmanager.utils.PasswordUtil; // Removed unused import
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder; // Added import
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.taskmanager.taskmanager.model.User;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private PasswordEncoder passwordEncoder; // Added PasswordEncoder

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody AuthRequest authRequest) {
        // Validate request
        if (authRequest.getUsername() == null || authRequest.getPassword() == null || authRequest.getEmail() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username, password and email are required"));
        }

        User user = new User();
        user.setUsername(authRequest.getUsername());
        // Hash the password before saving
        user.setPassword(passwordEncoder.encode(authRequest.getPassword()));
        user.setEmail(authRequest.getEmail());

        if (userService.findUserByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username already exists"));
        }

        Optional<User> userByEmail = userService.findUserByEmail(user.getEmail());
        if (userByEmail.isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already exists"));
        }

        User savedUser = userService.saveUser(user);

        // Generate JWT token
        final String jwt = jwtUtil.generateToken(savedUser.getUsername());

        // Return AuthResponse DTO
        AuthResponse response = new AuthResponse(jwt, savedUser.getId(), savedUser.getUsername(), savedUser.getEmail());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest authRequest) {
        try {
            // Authenticate user
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(authRequest.getUsername(), authRequest.getPassword())
            );

            // If authentication is successful, generate JWT token
            final String jwt = jwtUtil.generateToken(authRequest.getUsername());

            // Fetch user details for the response
            User user = userService.findUserByUsername(authRequest.getUsername())
                    .orElseThrow(() -> new BadCredentialsException("User not found after authentication")); // Should not happen if auth succeeds

            // Return AuthResponse DTO
            AuthResponse response = new AuthResponse(jwt, user.getId(), user.getUsername(), user.getEmail());
            return ResponseEntity.ok(response);

        } catch (BadCredentialsException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid username or password"));
        }
    }
}
