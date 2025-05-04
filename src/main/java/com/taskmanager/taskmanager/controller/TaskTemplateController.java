package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.TaskTemplateDto;
import com.taskmanager.taskmanager.model.Category;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.TaskTemplate;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.service.CategoryService;
import com.taskmanager.taskmanager.service.TaskTemplateService;
import com.taskmanager.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/templates")
public class TaskTemplateController {

    @Autowired
    private TaskTemplateService templateService;

    @Autowired
    private UserService userService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public ResponseEntity<?> getTemplates() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User user = userOptional.get();
        List<TaskTemplate> templates = templateService.getTemplatesByUserId(user.getId());
        List<TaskTemplateDto> templateDtos = templateService.convertToDtoList(templates);
        
        return ResponseEntity.ok(templateDtos);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getTemplateById(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<TaskTemplate> templateOptional = templateService.getTemplateById(id);
        if (templateOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TaskTemplate template = templateOptional.get();
        User user = userOptional.get();
        
        // Check if the user is the owner of the template
        if (!template.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to access this template"));
        }
        
        TaskTemplateDto templateDto = templateService.convertToDto(template);
        return ResponseEntity.ok(templateDto);
    }

    @PostMapping
    public ResponseEntity<?> createTemplate(@RequestBody TaskTemplateDto templateDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        User user = userOptional.get();
        
        TaskTemplate template = TaskTemplate.builder()
                .name(templateDto.getName())
                .description(templateDto.getDescription())
                .taskTitleTemplate(templateDto.getTaskTitleTemplate())
                .taskDescriptionTemplate(templateDto.getTaskDescriptionTemplate())
                .defaultPriority(templateDto.getDefaultPriority())
                .defaultDueDateDays(templateDto.getDefaultDueDateDays())
                .user(user)
                .categories(new ArrayList<>())
                .build();
        
        // Add categories if provided
        if (templateDto.getCategoryIds() != null && !templateDto.getCategoryIds().isEmpty()) {
            for (Long categoryId : templateDto.getCategoryIds()) {
                Optional<Category> categoryOptional = categoryService.getCategoryById(categoryId);
                if (categoryOptional.isPresent() && categoryOptional.get().getUser().getId().equals(user.getId())) {
                    template.getCategories().add(categoryOptional.get());
                }
            }
        }
        
        TaskTemplate savedTemplate = templateService.createTemplate(template);
        TaskTemplateDto responseDto = templateService.convertToDto(savedTemplate);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateTemplate(@PathVariable Long id, @RequestBody TaskTemplateDto templateDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<TaskTemplate> templateOptional = templateService.getTemplateById(id);
        if (templateOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TaskTemplate template = templateOptional.get();
        User user = userOptional.get();
        
        // Check if the user is the owner of the template
        if (!template.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to update this template"));
        }
        
        template.setName(templateDto.getName());
        template.setDescription(templateDto.getDescription());
        template.setTaskTitleTemplate(templateDto.getTaskTitleTemplate());
        template.setTaskDescriptionTemplate(templateDto.getTaskDescriptionTemplate());
        template.setDefaultPriority(templateDto.getDefaultPriority());
        template.setDefaultDueDateDays(templateDto.getDefaultDueDateDays());
        
        // Update categories
        if (templateDto.getCategoryIds() != null) {
            template.getCategories().clear();
            
            for (Long categoryId : templateDto.getCategoryIds()) {
                Optional<Category> categoryOptional = categoryService.getCategoryById(categoryId);
                if (categoryOptional.isPresent() && categoryOptional.get().getUser().getId().equals(user.getId())) {
                    template.getCategories().add(categoryOptional.get());
                }
            }
        }
        
        TaskTemplate updatedTemplate = templateService.updateTemplate(template);
        TaskTemplateDto responseDto = templateService.convertToDto(updatedTemplate);
        
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteTemplate(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<TaskTemplate> templateOptional = templateService.getTemplateById(id);
        if (templateOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TaskTemplate template = templateOptional.get();
        User user = userOptional.get();
        
        // Check if the user is the owner of the template
        if (!template.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to delete this template"));
        }
        
        templateService.deleteTemplate(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/create-task")
    public ResponseEntity<?> createTaskFromTemplate(@PathVariable Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<TaskTemplate> templateOptional = templateService.getTemplateById(id);
        if (templateOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TaskTemplate template = templateOptional.get();
        User user = userOptional.get();
        
        // Check if the user is the owner of the template
        if (!template.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to use this template"));
        }
        
        try {
            Task task = templateService.createTaskFromTemplate(id, user);
            return ResponseEntity.status(HttpStatus.CREATED).body(task);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }
}
