package com.taskmanager.taskmanager.security;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.MacAlgorithm;

import java.security.Key;
import java.util.Date;



public class JwtUtil {

    private static final String SECRET = "tGZ8uGc+Qk8lFpHqgY6q0xFSR8t0lZJ4Y9YmPz6z+Zg="; // base64-encoded key

    private static final MacAlgorithm ALGORITHM = io.jsonwebtoken.Jwts.SIG.HS256;

    private final Key key = ALGORITHM.key().build();


    public String generateToken(String username) {
        return Jwts.builder()
                .subject(username)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000 * 60 * 60 * 24))
                .signWith(key)
                .compact()
                ;
    }
}
