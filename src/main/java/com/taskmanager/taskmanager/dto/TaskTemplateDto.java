package com.taskmanager.taskmanager.dto;

import com.taskmanager.taskmanager.model.Priority;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaskTemplateDto {
    private Long id;
    private String name;
    private String description;
    private String taskTitleTemplate;
    private String taskDescriptionTemplate;
    private Priority defaultPriority;
    private Integer defaultDueDateDays;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
    private List<Long> categoryIds;
    private List<TemplateSubtaskDto> subtasks;
}
