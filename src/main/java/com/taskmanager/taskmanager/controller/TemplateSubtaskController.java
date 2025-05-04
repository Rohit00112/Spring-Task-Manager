package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.TemplateSubtaskDto;
import com.taskmanager.taskmanager.model.TaskTemplate;
import com.taskmanager.taskmanager.model.TemplateSubtask;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.service.TaskTemplateService;
import com.taskmanager.taskmanager.service.TemplateSubtaskService;
import com.taskmanager.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/templates/{templateId}/subtasks")
public class TemplateSubtaskController {

    @Autowired
    private TemplateSubtaskService subtaskService;

    @Autowired
    private TaskTemplateService templateService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createSubtask(@PathVariable Long templateId, @RequestBody TemplateSubtaskDto subtaskDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<TaskTemplate> templateOptional = templateService.getTemplateById(templateId);
        if (templateOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TaskTemplate template = templateOptional.get();
        User user = userOptional.get();
        
        // Check if the user is the owner of the template
        if (!template.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to modify this template"));
        }
        
        TemplateSubtask subtask = TemplateSubtask.builder()
                .title(subtaskDto.getTitle())
                .description(subtaskDto.getDescription())
                .position(subtaskDto.getPosition())
                .template(template)
                .build();
        
        TemplateSubtask savedSubtask = subtaskService.createSubtask(subtask);
        
        TemplateSubtaskDto responseDto = TemplateSubtaskDto.builder()
                .id(savedSubtask.getId())
                .title(savedSubtask.getTitle())
                .description(savedSubtask.getDescription())
                .position(savedSubtask.getPosition())
                .templateId(templateId)
                .build();
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<?> getSubtasks(@PathVariable Long templateId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<TaskTemplate> templateOptional = templateService.getTemplateById(templateId);
        if (templateOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TaskTemplate template = templateOptional.get();
        User user = userOptional.get();
        
        // Check if the user is the owner of the template
        if (!template.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to access this template"));
        }
        
        List<TemplateSubtask> subtasks = subtaskService.getSubtasksByTemplateId(templateId);
        List<TemplateSubtaskDto> subtaskDtos = subtasks.stream()
                .map(subtask -> TemplateSubtaskDto.builder()
                        .id(subtask.getId())
                        .title(subtask.getTitle())
                        .description(subtask.getDescription())
                        .position(subtask.getPosition())
                        .templateId(templateId)
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(subtaskDtos);
    }

    @GetMapping("/{subtaskId}")
    public ResponseEntity<?> getSubtask(@PathVariable Long templateId, @PathVariable Long subtaskId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<TaskTemplate> templateOptional = templateService.getTemplateById(templateId);
        if (templateOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TaskTemplate template = templateOptional.get();
        User user = userOptional.get();
        
        // Check if the user is the owner of the template
        if (!template.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to access this template"));
        }
        
        Optional<TemplateSubtask> subtaskOptional = subtaskService.getSubtaskById(subtaskId);
        if (subtaskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TemplateSubtask subtask = subtaskOptional.get();
        if (!subtask.getTemplate().getId().equals(templateId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Subtask does not belong to the specified template"));
        }
        
        TemplateSubtaskDto subtaskDto = TemplateSubtaskDto.builder()
                .id(subtask.getId())
                .title(subtask.getTitle())
                .description(subtask.getDescription())
                .position(subtask.getPosition())
                .templateId(templateId)
                .build();
        
        return ResponseEntity.ok(subtaskDto);
    }

    @PutMapping("/{subtaskId}")
    public ResponseEntity<?> updateSubtask(@PathVariable Long templateId, @PathVariable Long subtaskId, @RequestBody TemplateSubtaskDto subtaskDto) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<TaskTemplate> templateOptional = templateService.getTemplateById(templateId);
        if (templateOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TaskTemplate template = templateOptional.get();
        User user = userOptional.get();
        
        // Check if the user is the owner of the template
        if (!template.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to modify this template"));
        }
        
        Optional<TemplateSubtask> subtaskOptional = subtaskService.getSubtaskById(subtaskId);
        if (subtaskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TemplateSubtask subtask = subtaskOptional.get();
        if (!subtask.getTemplate().getId().equals(templateId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Subtask does not belong to the specified template"));
        }
        
        subtask.setTitle(subtaskDto.getTitle());
        subtask.setDescription(subtaskDto.getDescription());
        subtask.setPosition(subtaskDto.getPosition());
        
        TemplateSubtask updatedSubtask = subtaskService.updateSubtask(subtask);
        
        TemplateSubtaskDto responseDto = TemplateSubtaskDto.builder()
                .id(updatedSubtask.getId())
                .title(updatedSubtask.getTitle())
                .description(updatedSubtask.getDescription())
                .position(updatedSubtask.getPosition())
                .templateId(templateId)
                .build();
        
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{subtaskId}")
    public ResponseEntity<?> deleteSubtask(@PathVariable Long templateId, @PathVariable Long subtaskId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<TaskTemplate> templateOptional = templateService.getTemplateById(templateId);
        if (templateOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TaskTemplate template = templateOptional.get();
        User user = userOptional.get();
        
        // Check if the user is the owner of the template
        if (!template.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to modify this template"));
        }
        
        Optional<TemplateSubtask> subtaskOptional = subtaskService.getSubtaskById(subtaskId);
        if (subtaskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TemplateSubtask subtask = subtaskOptional.get();
        if (!subtask.getTemplate().getId().equals(templateId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Subtask does not belong to the specified template"));
        }
        
        subtaskService.deleteSubtask(subtaskId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reorder")
    public ResponseEntity<?> reorderSubtasks(@PathVariable Long templateId, @RequestBody List<Long> subtaskIds) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<TaskTemplate> templateOptional = templateService.getTemplateById(templateId);
        if (templateOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        TaskTemplate template = templateOptional.get();
        User user = userOptional.get();
        
        // Check if the user is the owner of the template
        if (!template.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to modify this template"));
        }
        
        List<TemplateSubtask> reorderedSubtasks = subtaskService.reorderSubtasks(templateId, subtaskIds);
        List<TemplateSubtaskDto> subtaskDtos = reorderedSubtasks.stream()
                .map(subtask -> TemplateSubtaskDto.builder()
                        .id(subtask.getId())
                        .title(subtask.getTitle())
                        .description(subtask.getDescription())
                        .position(subtask.getPosition())
                        .templateId(templateId)
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(subtaskDtos);
    }
}
