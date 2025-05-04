package com.taskmanager.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubtaskDto {
    private Long id;
    private String title;
    private String description;
    private boolean completed;
    private Integer position;
    private LocalDateTime createdAt;
    private LocalDateTime completedAt;
    private Long taskId;
}
