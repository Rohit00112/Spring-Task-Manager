package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.TaskTemplateDto;
import com.taskmanager.taskmanager.dto.TemplateSubtaskDto;
import com.taskmanager.taskmanager.model.*;
import com.taskmanager.taskmanager.repository.TaskTemplateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TaskTemplateService {

    @Autowired
    private TaskTemplateRepository templateRepository;

    @Autowired
    private TemplateSubtaskService subtaskService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private SubtaskService subtaskService2;

    /**
     * Create a new task template
     * 
     * @param template The template to create
     * @return The created template
     */
    @Transactional
    public TaskTemplate createTemplate(TaskTemplate template) {
        template.setCreatedAt(LocalDateTime.now());
        return templateRepository.save(template);
    }

    /**
     * Get all templates for a user
     * 
     * @param userId The ID of the user
     * @return A list of templates
     */
    public List<TaskTemplate> getTemplatesByUserId(Long userId) {
        return templateRepository.findByUserIdOrderByNameAsc(userId);
    }

    /**
     * Get a template by ID
     * 
     * @param templateId The ID of the template
     * @return The template, if found
     */
    public Optional<TaskTemplate> getTemplateById(Long templateId) {
        return templateRepository.findById(templateId);
    }

    /**
     * Update a template
     * 
     * @param template The template to update
     * @return The updated template
     */
    @Transactional
    public TaskTemplate updateTemplate(TaskTemplate template) {
        template.setUpdatedAt(LocalDateTime.now());
        return templateRepository.save(template);
    }

    /**
     * Delete a template
     * 
     * @param templateId The ID of the template to delete
     */
    @Transactional
    public void deleteTemplate(Long templateId) {
        templateRepository.deleteById(templateId);
    }

    /**
     * Create a task from a template
     * 
     * @param templateId The ID of the template
     * @param user The user creating the task
     * @return The created task
     */
    @Transactional
    public Task createTaskFromTemplate(Long templateId, User user) {
        Optional<TaskTemplate> templateOptional = getTemplateById(templateId);
        if (templateOptional.isEmpty()) {
            throw new IllegalArgumentException("Template not found");
        }
        
        TaskTemplate template = templateOptional.get();
        
        // Create the task
        Task task = Task.builder()
                .title(template.getTaskTitleTemplate() != null ? template.getTaskTitleTemplate() : template.getName())
                .description(template.getTaskDescriptionTemplate() != null ? template.getTaskDescriptionTemplate() : template.getDescription())
                .status(TaskStatus.TODO)
                .priority(template.getDefaultPriority() != null ? template.getDefaultPriority() : Priority.MEDIUM)
                .dueDate(template.getDefaultDueDateDays() != null ? LocalDate.now().plusDays(template.getDefaultDueDateDays()) : LocalDate.now().plusDays(7))
                .completed(false)
                .user(user)
                .build();
        
        // Add categories
        if (template.getCategories() != null && !template.getCategories().isEmpty()) {
            task.getCategories().addAll(template.getCategories());
        }
        
        // Save the task
        Task savedTask = taskService.createTask(task);
        
        // Create subtasks
        List<TemplateSubtask> templateSubtasks = subtaskService.getSubtasksByTemplateId(templateId);
        for (TemplateSubtask templateSubtask : templateSubtasks) {
            Subtask subtask = Subtask.builder()
                    .title(templateSubtask.getTitle())
                    .description(templateSubtask.getDescription())
                    .position(templateSubtask.getPosition())
                    .completed(false)
                    .task(savedTask)
                    .build();
            
            subtaskService2.createSubtask(subtask);
        }
        
        return savedTask;
    }
    
    /**
     * Convert a TaskTemplate to a TaskTemplateDto
     * 
     * @param template The template to convert
     * @return The template DTO
     */
    public TaskTemplateDto convertToDto(TaskTemplate template) {
        List<TemplateSubtaskDto> subtaskDtos = subtaskService.getSubtasksByTemplateId(template.getId()).stream()
                .map(subtask -> TemplateSubtaskDto.builder()
                        .id(subtask.getId())
                        .title(subtask.getTitle())
                        .description(subtask.getDescription())
                        .position(subtask.getPosition())
                        .templateId(template.getId())
                        .build())
                .collect(Collectors.toList());
        
        List<Long> categoryIds = template.getCategories().stream()
                .map(Category::getId)
                .collect(Collectors.toList());
        
        return TaskTemplateDto.builder()
                .id(template.getId())
                .name(template.getName())
                .description(template.getDescription())
                .taskTitleTemplate(template.getTaskTitleTemplate())
                .taskDescriptionTemplate(template.getTaskDescriptionTemplate())
                .defaultPriority(template.getDefaultPriority())
                .defaultDueDateDays(template.getDefaultDueDateDays())
                .createdAt(template.getCreatedAt())
                .updatedAt(template.getUpdatedAt())
                .userId(template.getUser().getId())
                .categoryIds(categoryIds)
                .subtasks(subtaskDtos)
                .build();
    }
    
    /**
     * Convert a list of TaskTemplates to TaskTemplateDtos
     * 
     * @param templates The list of templates to convert
     * @return The list of template DTOs
     */
    public List<TaskTemplateDto> convertToDtoList(List<TaskTemplate> templates) {
        return templates.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }
}
