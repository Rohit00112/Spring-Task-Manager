package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.Task;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface TaskRepository extends Repository<Task, Long> {
    Task save(Task task);
    List<Task> findByUserId(Long userId);
    Task findById(Long id);
    void deleteById(Long id);
    List<Task> findAll();

}
