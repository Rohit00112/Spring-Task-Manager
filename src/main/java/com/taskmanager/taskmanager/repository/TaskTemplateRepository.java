package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.TaskTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TaskTemplateRepository extends JpaRepository<TaskTemplate, Long> {
    List<TaskTemplate> findByUserId(Long userId);
    List<TaskTemplate> findByUserIdOrderByNameAsc(Long userId);
}
