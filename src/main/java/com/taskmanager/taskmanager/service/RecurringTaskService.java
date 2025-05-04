package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.model.RecurrencePattern;
import com.taskmanager.taskmanager.model.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class RecurringTaskService {

    @Autowired
    private TaskService taskService;

    /**
     * Creates a new task instance based on a recurring task pattern
     * 
     * @param task The original recurring task
     * @return The newly created task instance
     */
    @Transactional
    public Task createRecurringTaskInstance(Task task) {
        if (!task.isRecurring()) {
            throw new IllegalArgumentException("Task is not recurring");
        }
        
        // Create a new task instance based on the recurring task
        Task newTask = Task.builder()
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .priority(task.getPriority())
                .user(task.getUser())
                .categories(new ArrayList<>(task.getCategories()))
                .parentTaskId(task.getId())
                .completed(false)
                .build();
        
        // Calculate the next due date based on the recurrence pattern
        LocalDate nextDueDate = calculateNextDueDate(task);
        newTask.setDueDate(nextDueDate);
        
        // If there's a reminder, calculate it relative to the new due date
        if (task.getReminderDate() != null) {
            long daysBefore = task.getDueDate().toEpochDay() - task.getReminderDate().toEpochDay();
            newTask.setReminderDate(nextDueDate.minusDays(daysBefore));
        }
        
        return taskService.createTask(newTask);
    }
    
    /**
     * Calculates the next due date based on the recurrence pattern
     * 
     * @param task The recurring task
     * @return The next due date
     */
    private LocalDate calculateNextDueDate(Task task) {
        LocalDate currentDueDate = task.getDueDate();
        RecurrencePattern pattern = task.getRecurrencePattern();
        Integer interval = task.getRecurrenceInterval() != null ? task.getRecurrenceInterval() : 1;
        
        switch (pattern) {
            case DAILY:
                return currentDueDate.plusDays(interval);
            case WEEKLY:
                return currentDueDate.plusWeeks(interval);
            case BIWEEKLY:
                return currentDueDate.plusWeeks(2 * interval);
            case MONTHLY:
                return currentDueDate.plusMonths(interval);
            case YEARLY:
                return currentDueDate.plusYears(interval);
            case CUSTOM:
                // For custom patterns, default to daily
                return currentDueDate.plusDays(interval);
            default:
                return currentDueDate.plusDays(1);
        }
    }
    
    /**
     * Scheduled job to create instances of recurring tasks
     * Runs daily at midnight
     */
    @Scheduled(cron = "0 0 0 * * ?")
    @Transactional
    public void processRecurringTasks() {
        // Get all recurring tasks that are due today or in the past and not completed
        List<Task> recurringTasks = taskService.findRecurringTasksDueToday();
        
        for (Task task : recurringTasks) {
            // Skip if the recurrence end date has passed
            if (task.getRecurrenceEndDate() != null && 
                task.getRecurrenceEndDate().isBefore(LocalDate.now())) {
                continue;
            }
            
            // Create a new instance of the recurring task
            createRecurringTaskInstance(task);
            
            // Mark the current task as completed
            task.setCompleted(true);
            taskService.updateTask(task);
        }
    }
}
