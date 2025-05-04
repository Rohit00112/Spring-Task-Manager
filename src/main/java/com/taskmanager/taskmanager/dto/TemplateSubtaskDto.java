package com.taskmanager.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateSubtaskDto {
    private Long id;
    private String title;
    private String description;
    private Integer position;
    private Long templateId;
}
