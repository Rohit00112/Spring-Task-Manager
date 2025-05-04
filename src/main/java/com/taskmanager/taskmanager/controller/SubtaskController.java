package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.SubtaskDto;
import com.taskmanager.taskmanager.model.Subtask;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.service.SubtaskService;
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
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks/{taskId}/subtasks")
public class SubtaskController {

    @Autowired
    private SubtaskService subtaskService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createSubtask(@PathVariable Long taskId, @RequestBody SubtaskDto subtaskDto) {
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
        if (!task.getUser().getId().equals(userOptional.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to access this task"));
        }
        
        Subtask subtask = Subtask.builder()
                .title(subtaskDto.getTitle())
                .description(subtaskDto.getDescription())
                .completed(subtaskDto.isCompleted())
                .position(subtaskDto.getPosition())
                .task(task)
                .build();
        
        Subtask savedSubtask = subtaskService.createSubtask(subtask);
        
        SubtaskDto responseDto = mapToDto(savedSubtask);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<?> getSubtasks(@PathVariable Long taskId) {
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
        if (!task.getUser().getId().equals(userOptional.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to access this task"));
        }
        
        List<Subtask> subtasks = subtaskService.getSubtasksByTaskId(taskId);
        List<SubtaskDto> subtaskDtos = subtasks.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(subtaskDtos);
    }

    @GetMapping("/{subtaskId}")
    public ResponseEntity<?> getSubtask(@PathVariable Long taskId, @PathVariable Long subtaskId) {
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
        if (!task.getUser().getId().equals(userOptional.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to access this task"));
        }
        
        Optional<Subtask> subtaskOptional = subtaskService.getSubtaskById(subtaskId);
        if (subtaskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Subtask subtask = subtaskOptional.get();
        if (!subtask.getTask().getId().equals(taskId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Subtask does not belong to the specified task"));
        }
        
        SubtaskDto subtaskDto = mapToDto(subtask);
        return ResponseEntity.ok(subtaskDto);
    }

    @PutMapping("/{subtaskId}")
    public ResponseEntity<?> updateSubtask(@PathVariable Long taskId, @PathVariable Long subtaskId, @RequestBody SubtaskDto subtaskDto) {
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
        if (!task.getUser().getId().equals(userOptional.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to access this task"));
        }
        
        Optional<Subtask> subtaskOptional = subtaskService.getSubtaskById(subtaskId);
        if (subtaskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Subtask subtask = subtaskOptional.get();
        if (!subtask.getTask().getId().equals(taskId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Subtask does not belong to the specified task"));
        }
        
        subtask.setTitle(subtaskDto.getTitle());
        subtask.setDescription(subtaskDto.getDescription());
        subtask.setCompleted(subtaskDto.isCompleted());
        subtask.setPosition(subtaskDto.getPosition());
        
        Subtask updatedSubtask = subtaskService.updateSubtask(subtask);
        
        // Update task completion status based on subtasks
        boolean allCompleted = subtaskService.updateTaskCompletionStatus(task);
        if (allCompleted != task.isCompleted()) {
            taskService.updateTask(task);
        }
        
        SubtaskDto responseDto = mapToDto(updatedSubtask);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{subtaskId}")
    public ResponseEntity<?> deleteSubtask(@PathVariable Long taskId, @PathVariable Long subtaskId) {
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
        if (!task.getUser().getId().equals(userOptional.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to access this task"));
        }
        
        Optional<Subtask> subtaskOptional = subtaskService.getSubtaskById(subtaskId);
        if (subtaskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Subtask subtask = subtaskOptional.get();
        if (!subtask.getTask().getId().equals(taskId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Subtask does not belong to the specified task"));
        }
        
        subtaskService.deleteSubtask(subtaskId);
        
        // Update task completion status based on remaining subtasks
        subtaskService.updateTaskCompletionStatus(task);
        taskService.updateTask(task);
        
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reorder")
    public ResponseEntity<?> reorderSubtasks(@PathVariable Long taskId, @RequestBody List<Long> subtaskIds) {
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
        if (!task.getUser().getId().equals(userOptional.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to access this task"));
        }
        
        List<Subtask> reorderedSubtasks = subtaskService.reorderSubtasks(taskId, subtaskIds);
        List<SubtaskDto> subtaskDtos = reorderedSubtasks.stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(subtaskDtos);
    }
    
    /**
     * Map a Subtask entity to a SubtaskDto
     * 
     * @param subtask The subtask entity
     * @return The subtask DTO
     */
    private SubtaskDto mapToDto(Subtask subtask) {
        return SubtaskDto.builder()
                .id(subtask.getId())
                .title(subtask.getTitle())
                .description(subtask.getDescription())
                .completed(subtask.isCompleted())
                .position(subtask.getPosition())
                .createdAt(subtask.getCreatedAt())
                .completedAt(subtask.getCompletedAt())
                .taskId(subtask.getTask().getId())
                .build();
    }
}
