package com.taskmanager.taskmanager.dto;

import com.taskmanager.taskmanager.model.CollaboratorRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CollaboratorDto {
    private Long id;
    private Long taskId;
    private Long userId;
    private String username;
    private String email;
    private CollaboratorRole role;
    private LocalDateTime addedAt;
    private Long addedById;
    private String addedByUsername;
}
