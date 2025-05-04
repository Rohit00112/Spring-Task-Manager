package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.TaskDto;
import com.taskmanager.taskmanager.model.Category;
import com.taskmanager.taskmanager.model.CollaboratorRole;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.service.CategoryService;
import com.taskmanager.taskmanager.service.CollaboratorService;
import com.taskmanager.taskmanager.service.RecurringTaskService;
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
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @GetMapping
    public ResponseEntity<List<Task>> getAllTasks() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userOptional.get();
        List<Task> tasks = taskService.getTaskByUserId(user.getId());
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTaskById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Task> taskOptional = taskService.getTaskById(id);
        if (taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Task task = taskOptional.get();
        User user = userOptional.get();

        // Check if the user is the owner or a collaborator
        boolean isOwner = task.getUser().getId().equals(user.getId());
        boolean isCollaborator = collaboratorService.hasAccess(id, user.getId());

        if (!isOwner && !isCollaborator) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to access this task"));
        }

        return ResponseEntity.ok(task);
    }

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private RecurringTaskService recurringTaskService;

    @Autowired
    private CollaboratorService collaboratorService;

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TaskDto taskDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        User user = userOptional.get();

        Task task = Task.builder()
                .title(taskDto.getTitle())
                .description(taskDto.getDescription())
                .status(taskDto.getStatus())
                .priority(taskDto.getPriority())
                .dueDate(taskDto.getDueDate())
                .reminderDate(taskDto.getReminderDate())
                .completed(taskDto.isCompleted())
                .user(user)
                .build();

        // Set recurrence fields if this is a recurring task
        if (taskDto.isRecurring()) {
            task.setRecurring(true);
            task.setRecurrencePattern(taskDto.getRecurrencePattern());
            task.setRecurrenceInterval(taskDto.getRecurrenceInterval());
            task.setRecurrenceEndDate(taskDto.getRecurrenceEndDate());
        }

        // Add categories if provided
        if (taskDto.getCategoryIds() != null && !taskDto.getCategoryIds().isEmpty()) {
            for (Long categoryId : taskDto.getCategoryIds()) {
                Optional<Category> categoryOptional = categoryService.getCategoryById(categoryId);
                if (categoryOptional.isPresent() && categoryOptional.get().getUser().getId().equals(user.getId())) {
                    task.getCategories().add(categoryOptional.get());
                }
            }
        }

        Task savedTask = taskService.createTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedTask);
    }

    @PostMapping("/{id}/create-instance")
    public ResponseEntity<?> createRecurringTaskInstance(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Task> taskOptional = taskService.getTaskById(id);
        if (taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Task task = taskOptional.get();
        if (!task.getUser().getId().equals(userOptional.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to access this task"));
        }

        if (!task.isRecurring()) {
            return ResponseEntity.badRequest().body(Map.of("error", "This is not a recurring task"));
        }

        try {
            Task newInstance = recurringTaskService.createRecurringTaskInstance(task);
            return ResponseEntity.status(HttpStatus.CREATED).body(newInstance);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{id}/instances")
    public ResponseEntity<?> getRecurringTaskInstances(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Task> taskOptional = taskService.getTaskById(id);
        if (taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Task task = taskOptional.get();
        if (!task.getUser().getId().equals(userOptional.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to access this task"));
        }

        List<Task> instances = taskService.findTaskInstancesByParentId(id);
        return ResponseEntity.ok(instances);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTask(@PathVariable Long id, @RequestBody TaskDto taskDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Task> taskOptional = taskService.getTaskById(id);
        if (taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Task existingTask = taskOptional.get();
        User user = userOptional.get();

        // Check if the user is the owner or has edit permissions
        boolean isOwner = existingTask.getUser().getId().equals(user.getId());
        boolean canEdit = collaboratorService.hasRole(id, user.getId(), CollaboratorRole.EDITOR);

        if (!isOwner && !canEdit) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to update this task"));
        }

        existingTask.setTitle(taskDto.getTitle());
        existingTask.setDescription(taskDto.getDescription());
        existingTask.setStatus(taskDto.getStatus());
        existingTask.setPriority(taskDto.getPriority());
        existingTask.setDueDate(taskDto.getDueDate());
        existingTask.setReminderDate(taskDto.getReminderDate());
        existingTask.setCompleted(taskDto.isCompleted());

        // Update recurrence fields
        existingTask.setRecurring(taskDto.isRecurring());
        if (taskDto.isRecurring()) {
            existingTask.setRecurrencePattern(taskDto.getRecurrencePattern());
            existingTask.setRecurrenceInterval(taskDto.getRecurrenceInterval());
            existingTask.setRecurrenceEndDate(taskDto.getRecurrenceEndDate());
        }

        // Update categories if provided
        if (taskDto.getCategoryIds() != null) {
            existingTask.getCategories().clear();

            for (Long categoryId : taskDto.getCategoryIds()) {
                Optional<Category> categoryOptional = categoryService.getCategoryById(categoryId);
                if (categoryOptional.isPresent() && categoryOptional.get().getUser().getId().equals(userOptional.get().getId())) {
                    existingTask.getCategories().add(categoryOptional.get());
                }
            }
        }

        Task updatedTask = taskService.updateTask(existingTask);
        return ResponseEntity.ok(updatedTask);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTask(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Optional<Task> taskOptional = taskService.getTaskById(id);
        if (taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        Task task = taskOptional.get();
        User user = userOptional.get();

        // Check if the user is the owner or has admin permissions
        boolean isOwner = task.getUser().getId().equals(user.getId());
        boolean isAdmin = collaboratorService.hasRole(id, user.getId(), CollaboratorRole.ADMIN);

        if (!isOwner && !isAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to delete this task"));
        }

        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }
}
