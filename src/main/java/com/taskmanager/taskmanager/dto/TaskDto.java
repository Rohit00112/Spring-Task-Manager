package com.taskmanager.taskmanager.dto;

import com.taskmanager.taskmanager.model.Priority;
import com.taskmanager.taskmanager.model.RecurrencePattern;
import com.taskmanager.taskmanager.model.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskDto {
    private String title;
    private String description;
    private TaskStatus status;
    private Priority priority;
    private LocalDate dueDate;
    private LocalDate reminderDate;
    private boolean completed;
    private List<Long> categoryIds;

    // Recurrence fields
    private boolean recurring;
    private RecurrencePattern recurrencePattern;
    private Integer recurrenceInterval;
    private LocalDate recurrenceEndDate;
    private Long parentTaskId;
}
