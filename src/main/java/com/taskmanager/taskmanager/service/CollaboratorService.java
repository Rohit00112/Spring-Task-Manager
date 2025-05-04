package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.CollaboratorDto;
import com.taskmanager.taskmanager.model.CollaboratorRole;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.TaskCollaborator;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.repository.TaskCollaboratorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CollaboratorService {

    @Autowired
    private TaskCollaboratorRepository collaboratorRepository;

    /**
     * Add a collaborator to a task
     * 
     * @param task The task
     * @param user The user to add as a collaborator
     * @param role The role of the collaborator
     * @param addedBy The user who is adding the collaborator
     * @return The created collaborator
     */
    @Transactional
    public TaskCollaborator addCollaborator(Task task, User user, CollaboratorRole role, User addedBy) {
        // Check if the user is already a collaborator
        if (collaboratorRepository.existsByTaskIdAndUserId(task.getId(), user.getId())) {
            throw new IllegalArgumentException("User is already a collaborator for this task");
        }
        
        // Check if the user is the owner of the task
        if (task.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Cannot add the task owner as a collaborator");
        }
        
        TaskCollaborator collaborator = TaskCollaborator.builder()
                .task(task)
                .user(user)
                .role(role)
                .addedAt(LocalDateTime.now())
                .addedBy(addedBy)
                .build();
        
        return collaboratorRepository.save(collaborator);
    }

    /**
     * Get all collaborators for a task
     * 
     * @param taskId The ID of the task
     * @return A list of collaborators
     */
    public List<TaskCollaborator> getCollaboratorsByTaskId(Long taskId) {
        return collaboratorRepository.findByTaskId(taskId);
    }

    /**
     * Get all tasks that a user is a collaborator on
     * 
     * @param userId The ID of the user
     * @return A list of collaborators
     */
    public List<TaskCollaborator> getCollaborationsByUserId(Long userId) {
        return collaboratorRepository.findByUserId(userId);
    }

    /**
     * Get a collaborator by task ID and user ID
     * 
     * @param taskId The ID of the task
     * @param userId The ID of the user
     * @return The collaborator, if found
     */
    public Optional<TaskCollaborator> getCollaborator(Long taskId, Long userId) {
        return collaboratorRepository.findByTaskIdAndUserId(taskId, userId);
    }

    /**
     * Update a collaborator's role
     * 
     * @param collaborator The collaborator to update
     * @param role The new role
     * @return The updated collaborator
     */
    @Transactional
    public TaskCollaborator updateCollaboratorRole(TaskCollaborator collaborator, CollaboratorRole role) {
        collaborator.setRole(role);
        return collaboratorRepository.save(collaborator);
    }

    /**
     * Remove a collaborator from a task
     * 
     * @param collaboratorId The ID of the collaborator to remove
     */
    @Transactional
    public void removeCollaborator(Long collaboratorId) {
        collaboratorRepository.deleteById(collaboratorId);
    }
    
    /**
     * Check if a user has access to a task
     * 
     * @param taskId The ID of the task
     * @param userId The ID of the user
     * @return true if the user has access, false otherwise
     */
    public boolean hasAccess(Long taskId, Long userId) {
        return collaboratorRepository.existsByTaskIdAndUserId(taskId, userId);
    }
    
    /**
     * Check if a user has a specific role or higher for a task
     * 
     * @param taskId The ID of the task
     * @param userId The ID of the user
     * @param minimumRole The minimum role required
     * @return true if the user has the required role or higher, false otherwise
     */
    public boolean hasRole(Long taskId, Long userId, CollaboratorRole minimumRole) {
        Optional<TaskCollaborator> collaboratorOptional = getCollaborator(taskId, userId);
        if (collaboratorOptional.isEmpty()) {
            return false;
        }
        
        TaskCollaborator collaborator = collaboratorOptional.get();
        CollaboratorRole userRole = collaborator.getRole();
        
        // Check if the user's role is at least the minimum required role
        switch (minimumRole) {
            case VIEWER:
                return true; // Any role is sufficient
            case EDITOR:
                return userRole == CollaboratorRole.EDITOR || userRole == CollaboratorRole.ADMIN;
            case ADMIN:
                return userRole == CollaboratorRole.ADMIN;
            default:
                return false;
        }
    }
    
    /**
     * Convert a TaskCollaborator to a CollaboratorDto
     * 
     * @param collaborator The collaborator to convert
     * @return The collaborator DTO
     */
    public CollaboratorDto convertToDto(TaskCollaborator collaborator) {
        return CollaboratorDto.builder()
                .id(collaborator.getId())
                .taskId(collaborator.getTask().getId())
                .userId(collaborator.getUser().getId())
                .username(collaborator.getUser().getUsername())
                .email(collaborator.getUser().getEmail())
                .role(collaborator.getRole())
                .addedAt(collaborator.getAddedAt())
                .addedById(collaborator.getAddedBy().getId())
                .addedByUsername(collaborator.getAddedBy().getUsername())
                .build();
    }
    
    /**
     * Convert a list of TaskCollaborators to CollaboratorDtos
     * 
     * @param collaborators The list of collaborators to convert
     * @return The list of collaborator DTOs
     */
    public List<CollaboratorDto> convertToDtoList(List<TaskCollaborator> collaborators) {
        return collaborators.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}
