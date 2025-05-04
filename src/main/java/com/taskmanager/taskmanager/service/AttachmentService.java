package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.model.Attachment;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class AttachmentService {

    @Autowired
    private AttachmentRepository attachmentRepository;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Save an attachment for a task
     * 
     * @param file The file to attach
     * @param task The task to attach the file to
     * @param user The user uploading the file
     * @return The saved attachment
     * @throws IOException If an error occurs during file storage
     */
    @Transactional
    public Attachment saveAttachment(MultipartFile file, Task task, User user) throws IOException {
        String fileName = fileStorageService.storeFile(file);
        
        Attachment attachment = Attachment.builder()
                .fileName(file.getOriginalFilename())
                .fileType(file.getContentType())
                .filePath(fileName)
                .fileSize(file.getSize())
                .uploadDate(LocalDateTime.now())
                .task(task)
                .user(user)
                .build();
        
        return attachmentRepository.save(attachment);
    }
    
    /**
     * Get all attachments for a task
     * 
     * @param taskId The ID of the task
     * @return A list of attachments
     */
    public List<Attachment> getAttachmentsByTaskId(Long taskId) {
        return attachmentRepository.findByTaskId(taskId);
    }
    
    /**
     * Get an attachment by ID
     * 
     * @param attachmentId The ID of the attachment
     * @return The attachment, if found
     */
    public Optional<Attachment> getAttachmentById(Long attachmentId) {
        return attachmentRepository.findById(attachmentId);
    }
    
    /**
     * Delete an attachment
     * 
     * @param attachment The attachment to delete
     * @return true if the attachment was deleted successfully, false otherwise
     */
    @Transactional
    public boolean deleteAttachment(Attachment attachment) {
        boolean fileDeleted = fileStorageService.deleteFile(attachment.getFilePath());
        if (fileDeleted) {
            attachmentRepository.delete(attachment);
            return true;
        }
        return false;
    }
}
