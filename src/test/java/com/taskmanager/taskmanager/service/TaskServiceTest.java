package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.model.Priority;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.TaskStatus;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private Task testTask;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .email("test@example.com")
                .build();

        testTask = Task.builder()
                .id(1L)
                .title("Test Task")
                .description("Test Description")
                .status(TaskStatus.TODO)
                .priority(Priority.MEDIUM)
                .dueDate(LocalDate.now().plusDays(1))
                .user(testUser)
                .build();
    }

    @Test
    void createTask() {
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task savedTask = taskService.createTask(testTask);

        assertEquals(testTask.getId(), savedTask.getId());
        assertEquals(testTask.getTitle(), savedTask.getTitle());
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    void getTaskByUserId() {
        when(taskRepository.findByUserId(testUser.getId())).thenReturn(Arrays.asList(testTask));

        List<Task> tasks = taskService.getTaskByUserId(testUser.getId());

        assertEquals(1, tasks.size());
        assertEquals(testTask.getId(), tasks.get(0).getId());
        verify(taskRepository, times(1)).findByUserId(testUser.getId());
    }

    @Test
    void getTaskById() {
        when(taskRepository.findById(testTask.getId())).thenReturn(Optional.of(testTask));

        Optional<Task> foundTask = taskService.getTaskById(testTask.getId());

        assertTrue(foundTask.isPresent());
        assertEquals(testTask.getId(), foundTask.get().getId());
        verify(taskRepository, times(1)).findById(testTask.getId());
    }
}
