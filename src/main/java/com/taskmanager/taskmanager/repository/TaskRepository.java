package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.Task;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface TaskRepository extends Repository<Task, Long> {
    List<Task> findByUserId(Long userId);
}
