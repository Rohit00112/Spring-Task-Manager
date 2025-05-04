package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.Subtask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SubtaskRepository extends JpaRepository<Subtask, Long> {
    List<Subtask> findByTaskId(Long taskId);
    List<Subtask> findByTaskIdOrderByPositionAsc(Long taskId);
}
