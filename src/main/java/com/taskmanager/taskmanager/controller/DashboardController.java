package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.TaskStatus;
import com.taskmanager.taskmanager.model.User;
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

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @GetMapping("/stats")
    public ResponseEntity<?> getTaskStatistics() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User user = userOptional.get();
        List<Task> tasks = taskService.getTaskByUserId(user.getId());
        
        Map<String, Object> statistics = new HashMap<>();
        
        // Total tasks
        statistics.put("totalTasks", tasks.size());
        
        // Tasks by status
        Map<TaskStatus, Long> tasksByStatus = tasks.stream()
                .collect(Collectors.groupingBy(Task::getStatus, Collectors.counting()));
        statistics.put("tasksByStatus", tasksByStatus);
        
        // Overdue tasks
        long overdueTasks = tasks.stream()
                .filter(task -> task.getDueDate().isBefore(LocalDate.now()) && task.getStatus() != TaskStatus.COMPLETED)
                .count();
        statistics.put("overdueTasks", overdueTasks);
        
        // Tasks due today
        long tasksDueToday = tasks.stream()
                .filter(task -> task.getDueDate().isEqual(LocalDate.now()) && task.getStatus() != TaskStatus.COMPLETED)
                .count();
        statistics.put("tasksDueToday", tasksDueToday);
        
        // Tasks due this week
        LocalDate endOfWeek = LocalDate.now().plusDays(7);
        long tasksDueThisWeek = tasks.stream()
                .filter(task -> 
                    task.getDueDate().isAfter(LocalDate.now()) && 
                    task.getDueDate().isBefore(endOfWeek) && 
                    task.getStatus() != TaskStatus.COMPLETED)
                .count();
        statistics.put("tasksDueThisWeek", tasksDueThisWeek);
        
        return ResponseEntity.ok(statistics);
    }
}
