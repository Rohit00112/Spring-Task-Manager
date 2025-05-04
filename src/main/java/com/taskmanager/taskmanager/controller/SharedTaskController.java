package com.taskmanager.taskmanager.controller;

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/shared-tasks")
public class SharedTaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private CollaboratorService collaboratorService;

    /**
     * Get all tasks shared with the current user
     * 
     * @return A list of tasks
     */
    @GetMapping
    public ResponseEntity<?> getSharedTasks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User user = userOptional.get();
        
        // Get all collaborations for the current user
        List<TaskCollaborator> collaborations = collaboratorService.getCollaborationsByUserId(user.getId());
        
        // Get the tasks for each collaboration
        List<Task> sharedTasks = collaborations.stream()
                .map(TaskCollaborator::getTask)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(sharedTasks);
    }
}
