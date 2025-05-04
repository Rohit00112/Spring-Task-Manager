package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.AttachmentDto;
import com.taskmanager.taskmanager.model.Attachment;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.service.AttachmentService;
import com.taskmanager.taskmanager.service.FileStorageService;
import com.taskmanager.taskmanager.service.TaskService;
import com.taskmanager.taskmanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks/{taskId}/attachments")
public class AttachmentController {

    @Autowired
    private AttachmentService attachmentService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @Autowired
    private FileStorageService fileStorageService;

    @PostMapping
    public ResponseEntity<?> uploadAttachment(@PathVariable Long taskId,
                                             @RequestParam("file") MultipartFile file) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<Task> taskOptional = taskService.getTaskById(taskId);
        if (taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Task task = taskOptional.get();
        if (!task.getUser().getId().equals(userOptional.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to access this task"));
        }
        
        try {
            Attachment attachment = attachmentService.saveAttachment(file, task, userOptional.get());
            
            AttachmentDto attachmentDto = AttachmentDto.builder()
                    .id(attachment.getId())
                    .fileName(attachment.getFileName())
                    .fileType(attachment.getFileType())
                    .fileSize(attachment.getFileSize())
                    .uploadDate(attachment.getUploadDate())
                    .taskId(task.getId())
                    .build();
            
            return ResponseEntity.status(HttpStatus.CREATED).body(attachmentDto);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to upload file: " + e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAttachments(@PathVariable Long taskId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<Task> taskOptional = taskService.getTaskById(taskId);
        if (taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Task task = taskOptional.get();
        if (!task.getUser().getId().equals(userOptional.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to access this task"));
        }
        
        List<Attachment> attachments = attachmentService.getAttachmentsByTaskId(taskId);
        
        List<AttachmentDto> attachmentDtos = attachments.stream()
                .map(attachment -> AttachmentDto.builder()
                        .id(attachment.getId())
                        .fileName(attachment.getFileName())
                        .fileType(attachment.getFileType())
                        .fileSize(attachment.getFileSize())
                        .uploadDate(attachment.getUploadDate())
                        .taskId(task.getId())
                        .build())
                .collect(Collectors.toList());
        
        return ResponseEntity.ok(attachmentDtos);
    }

    @GetMapping("/{attachmentId}")
    public ResponseEntity<?> downloadAttachment(@PathVariable Long taskId,
                                              @PathVariable Long attachmentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<Task> taskOptional = taskService.getTaskById(taskId);
        if (taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Task task = taskOptional.get();
        if (!task.getUser().getId().equals(userOptional.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to access this task"));
        }
        
        Optional<Attachment> attachmentOptional = attachmentService.getAttachmentById(attachmentId);
        if (attachmentOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Attachment attachment = attachmentOptional.get();
        if (!attachment.getTask().getId().equals(taskId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Attachment does not belong to the specified task"));
        }
        
        try {
            Path filePath = fileStorageService.getFilePath(attachment.getFilePath());
            Resource resource = new UrlResource(filePath.toUri());
            
            if (resource.exists()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(attachment.getFileType()))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "File not found"));
            }
        } catch (MalformedURLException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to download file: " + e.getMessage()));
        }
    }

    @DeleteMapping("/{attachmentId}")
    public ResponseEntity<?> deleteAttachment(@PathVariable Long taskId,
                                            @PathVariable Long attachmentId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        Optional<User> userOptional = userService.findUserByUsername(username);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        
        Optional<Task> taskOptional = taskService.getTaskById(taskId);
        if (taskOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Task task = taskOptional.get();
        if (!task.getUser().getId().equals(userOptional.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to access this task"));
        }
        
        Optional<Attachment> attachmentOptional = attachmentService.getAttachmentById(attachmentId);
        if (attachmentOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Attachment attachment = attachmentOptional.get();
        if (!attachment.getTask().getId().equals(taskId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Attachment does not belong to the specified task"));
        }
        
        boolean deleted = attachmentService.deleteAttachment(attachment);
        if (deleted) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to delete attachment"));
        }
    }
}
