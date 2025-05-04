package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.CollaboratorDto;
import com.taskmanager.taskmanager.model.CollaboratorRole;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.TaskCollaborator;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.service.CollaboratorService;
import com.taskmanager.taskmanager.service.TaskService;
import com.taskmanager.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/tasks/{taskId}/collaborators")
public class CollaboratorController {

    @Autowired
    private CollaboratorService collaboratorService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> addCollaborator(@PathVariable Long taskId, @RequestBody Map<String, Object> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> currentUserOptional = userService.findUserByUsername(username);
        if (currentUserOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<Task> taskOptional = taskService.getTaskById(taskId);
        if (taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Task task = taskOptional.get();
        User currentUser = currentUserOptional.get();
        
        // Check if the current user is the task owner or an admin collaborator
        boolean isOwner = task.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = collaboratorService.hasRole(taskId, currentUser.getId(), CollaboratorRole.ADMIN);
        
        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You don't have permission to add collaborators to this task"));
        }
        
        // Get the user to add as a collaborator
        String collaboratorUsername = (String) request.get("username");
        if (collaboratorUsername == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Username is required"));
        }
        
        Optional<User> collaboratorOptional = userService.findUserByUsername(collaboratorUsername);
        if (collaboratorOptional.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "User not found"));
        }
        
        User collaborator = collaboratorOptional.get();
        
        // Get the role for the collaborator
        String roleStr = (String) request.get("role");
        if (roleStr == null) {
            roleStr = "VIEWER"; // Default role
        }
        
        CollaboratorRole role;
        try {
            role = CollaboratorRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
        }
        
        try {
            TaskCollaborator savedCollaborator = collaboratorService.addCollaborator(task, collaborator, role, currentUser);
            CollaboratorDto responseDto = collaboratorService.convertToDto(savedCollaborator);
            return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getCollaborators(@PathVariable Long taskId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<Task> taskOptional = taskService.getTaskById(taskId);
        if (taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Task task = taskOptional.get();
        User currentUser = userOptional.get();
        
        // Check if the current user is the task owner, a collaborator, or an admin
        boolean isOwner = task.getUser().getId().equals(currentUser.getId());
        boolean isCollaborator = collaboratorService.hasAccess(taskId, currentUser.getId());
        
        if (!isOwner && !isCollaborator) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You don't have permission to view collaborators for this task"));
        }
        
        List<TaskCollaborator> collaborators = collaboratorService.getCollaboratorsByTaskId(taskId);
        List<CollaboratorDto> collaboratorDtos = collaboratorService.convertToDtoList(collaborators);
        
        return ResponseEntity.ok(collaboratorDtos);
    }

    @PutMapping("/{collaboratorId}")
    public ResponseEntity<?> updateCollaboratorRole(@PathVariable Long taskId, 
                                                  @PathVariable Long collaboratorId, 
                                                  @RequestBody Map<String, String> request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> currentUserOptional = userService.findUserByUsername(username);
        if (currentUserOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<Task> taskOptional = taskService.getTaskById(taskId);
        if (taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Task task = taskOptional.get();
        User currentUser = currentUserOptional.get();
        
        // Check if the current user is the task owner or an admin collaborator
        boolean isOwner = task.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = collaboratorService.hasRole(taskId, currentUser.getId(), CollaboratorRole.ADMIN);
        
        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You don't have permission to update collaborators for this task"));
        }
        
        Optional<TaskCollaborator> collaboratorOptional = collaboratorService.getCollaborator(taskId, collaboratorId);
        if (collaboratorOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TaskCollaborator collaborator = collaboratorOptional.get();
        
        // Get the new role
        String roleStr = request.get("role");
        if (roleStr == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Role is required"));
        }
        
        CollaboratorRole role;
        try {
            role = CollaboratorRole.valueOf(roleStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid role"));
        }
        
        TaskCollaborator updatedCollaborator = collaboratorService.updateCollaboratorRole(collaborator, role);
        CollaboratorDto responseDto = collaboratorService.convertToDto(updatedCollaborator);
        
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{collaboratorId}")
    public ResponseEntity<?> removeCollaborator(@PathVariable Long taskId, @PathVariable Long collaboratorId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> currentUserOptional = userService.findUserByUsername(username);
        if (currentUserOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<Task> taskOptional = taskService.getTaskById(taskId);
        if (taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Task task = taskOptional.get();
        User currentUser = currentUserOptional.get();
        
        // Check if the current user is the task owner or an admin collaborator
        boolean isOwner = task.getUser().getId().equals(currentUser.getId());
        boolean isAdmin = collaboratorService.hasRole(taskId, currentUser.getId(), CollaboratorRole.ADMIN);
        
        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "You don't have permission to remove collaborators from this task"));
        }
        
        collaboratorService.removeCollaborator(collaboratorId);
        return ResponseEntity.noContent().build();
    }
}
