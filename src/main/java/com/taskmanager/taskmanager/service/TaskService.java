package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.model.TaskStatus;
import com.taskmanager.taskmanager.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.taskmanager.taskmanager.model.Task;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskService {

    @Autowired
    private TaskRepository taskRepository;

    @Transactional
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }

    public List<Task> getTaskByUserId(Long userId) {
        return taskRepository.findByUserId(userId);
    }

    public List<Task> getAllTasks() {
        return taskRepository.findAll();
    }

    @Transactional
    public void deleteTask(Long taskId) {
        taskRepository.deleteById(taskId);
    }

    @Transactional
    public Task updateTask(Task task) {
        return taskRepository.save(task);
    }

    public Optional<Task> getTaskById(Long taskId) {
        return taskRepository.findById(taskId);
    }

    /**
     * Find all recurring tasks that are due today or in the past and not completed
     *
     * @return List of recurring tasks
     */
    public List<Task> findRecurringTasksDueToday() {
        List<Task> allTasks = taskRepository.findAll();
        LocalDate today = LocalDate.now();

        return allTasks.stream()
                .filter(Task::isRecurring)
                .filter(task -> !task.isCompleted())
                .filter(task -> !task.getDueDate().isAfter(today))
                .collect(Collectors.toList());
    }

    /**
     * Find all tasks that are instances of a recurring parent task
     *
     * @param parentTaskId The ID of the parent recurring task
     * @return List of task instances
     */
    public List<Task> findTaskInstancesByParentId(Long parentTaskId) {
        List<Task> allTasks = taskRepository.findAll();

        return allTasks.stream()
                .filter(task -> parentTaskId.equals(task.getParentTaskId()))
                .collect(Collectors.toList());
    }
}
