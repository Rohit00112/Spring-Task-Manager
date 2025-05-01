package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.User;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface UserRepository extends Repository<User, Long> {
    Optional<User> findByUsername(String username);
}
