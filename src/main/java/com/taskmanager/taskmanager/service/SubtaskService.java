package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.model.Subtask;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.repository.SubtaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class SubtaskService {

    @Autowired
    private SubtaskRepository subtaskRepository;

    /**
     * Create a new subtask
     * 
     * @param subtask The subtask to create
     * @return The created subtask
     */
    @Transactional
    public Subtask createSubtask(Subtask subtask) {
        // Set creation time
        subtask.setCreatedAt(LocalDateTime.now());
        
        // Set position if not provided
        if (subtask.getPosition() == null) {
            List<Subtask> existingSubtasks = subtaskRepository.findByTaskId(subtask.getTask().getId());
            subtask.setPosition(existingSubtasks.size() + 1);
        }
        
        return subtaskRepository.save(subtask);
    }

    /**
     * Get all subtasks for a task
     * 
     * @param taskId The ID of the task
     * @return A list of subtasks
     */
    public List<Subtask> getSubtasksByTaskId(Long taskId) {
        return subtaskRepository.findByTaskIdOrderByPositionAsc(taskId);
    }

    /**
     * Get a subtask by ID
     * 
     * @param subtaskId The ID of the subtask
     * @return The subtask, if found
     */
    public Optional<Subtask> getSubtaskById(Long subtaskId) {
        return subtaskRepository.findById(subtaskId);
    }

    /**
     * Update a subtask
     * 
     * @param subtask The subtask to update
     * @return The updated subtask
     */
    @Transactional
    public Subtask updateSubtask(Subtask subtask) {
        // If the subtask is being marked as completed, set the completion time
        if (subtask.isCompleted() && subtask.getCompletedAt() == null) {
            subtask.setCompletedAt(LocalDateTime.now());
        } else if (!subtask.isCompleted()) {
            subtask.setCompletedAt(null);
        }
        
        return subtaskRepository.save(subtask);
    }

    /**
     * Delete a subtask
     * 
     * @param subtaskId The ID of the subtask to delete
     */
    @Transactional
    public void deleteSubtask(Long subtaskId) {
        subtaskRepository.deleteById(subtaskId);
    }

    /**
     * Reorder subtasks
     * 
     * @param taskId The ID of the task
     * @param subtaskIds The ordered list of subtask IDs
     * @return The reordered subtasks
     */
    @Transactional
    public List<Subtask> reorderSubtasks(Long taskId, List<Long> subtaskIds) {
        List<Subtask> subtasks = subtaskRepository.findByTaskId(taskId);
        
        for (int i = 0; i < subtaskIds.size(); i++) {
            Long subtaskId = subtaskIds.get(i);
            subtasks.stream()
                    .filter(subtask -> subtask.getId().equals(subtaskId))
                    .findFirst()
                    .ifPresent(subtask -> {
                        subtask.setPosition(i + 1);
                        subtaskRepository.save(subtask);
                    });
        }
        
        return subtaskRepository.findByTaskIdOrderByPositionAsc(taskId);
    }
    
    /**
     * Update task completion status based on subtasks
     * 
     * @param task The task to update
     * @return true if all subtasks are completed, false otherwise
     */
    @Transactional
    public boolean updateTaskCompletionStatus(Task task) {
        List<Subtask> subtasks = subtaskRepository.findByTaskId(task.getId());
        
        if (subtasks.isEmpty()) {
            return false;
        }
        
        boolean allCompleted = subtasks.stream().allMatch(Subtask::isCompleted);
        task.setCompleted(allCompleted);
        
        return allCompleted;
    }
}
