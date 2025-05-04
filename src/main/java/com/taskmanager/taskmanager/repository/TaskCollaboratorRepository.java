package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.TaskCollaborator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskCollaboratorRepository extends JpaRepository<TaskCollaborator, Long> {
    List<TaskCollaborator> findByTaskId(Long taskId);
    List<TaskCollaborator> findByUserId(Long userId);
    Optional<TaskCollaborator> findByTaskIdAndUserId(Long taskId, Long userId);
    boolean existsByTaskIdAndUserId(Long taskId, Long userId);
}
