package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.dto.CommentDto;
import com.taskmanager.taskmanager.model.Comment;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    /**
     * Create a new comment
     * 
     * @param comment The comment to create
     * @return The created comment
     */
    @Transactional
    public Comment createComment(Comment comment) {
        comment.setCreatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    /**
     * Get all comments for a task
     * 
     * @param taskId The ID of the task
     * @return A list of comments
     */
    public List<Comment> getCommentsByTaskId(Long taskId) {
        return commentRepository.findByTaskIdOrderByCreatedAtDesc(taskId);
    }
    
    /**
     * Get all top-level comments for a task (no replies)
     * 
     * @param taskId The ID of the task
     * @return A list of top-level comments
     */
    public List<Comment> getTopLevelCommentsByTaskId(Long taskId) {
        return commentRepository.findByTaskIdAndParentCommentIsNullOrderByCreatedAtDesc(taskId);
    }
    
    /**
     * Get all replies to a comment
     * 
     * @param parentId The ID of the parent comment
     * @return A list of reply comments
     */
    public List<Comment> getRepliesByParentId(Long parentId) {
        return commentRepository.findByParentCommentIdOrderByCreatedAtAsc(parentId);
    }

    /**
     * Get a comment by ID
     * 
     * @param commentId The ID of the comment
     * @return The comment, if found
     */
    public Optional<Comment> getCommentById(Long commentId) {
        return commentRepository.findById(commentId);
    }

    /**
     * Update a comment
     * 
     * @param comment The comment to update
     * @return The updated comment
     */
    @Transactional
    public Comment updateComment(Comment comment) {
        comment.setUpdatedAt(LocalDateTime.now());
        return commentRepository.save(comment);
    }

    /**
     * Delete a comment
     * 
     * @param commentId The ID of the comment to delete
     */
    @Transactional
    public void deleteComment(Long commentId) {
        commentRepository.deleteById(commentId);
    }
    
    /**
     * Convert comments to DTOs with nested replies
     * 
     * @param taskId The ID of the task
     * @return A list of comment DTOs with nested replies
     */
    public List<CommentDto> getCommentsWithReplies(Long taskId) {
        List<Comment> topLevelComments = getTopLevelCommentsByTaskId(taskId);
        return topLevelComments.stream()
                .map(comment -> convertToDto(comment, true))
                .collect(Collectors.toList());
    }
    
    /**
     * Convert a comment to a DTO, optionally including replies
     * 
     * @param comment The comment to convert
     * @param includeReplies Whether to include replies
     * @return The comment DTO
     */
    public CommentDto convertToDto(Comment comment, boolean includeReplies) {
        CommentDto dto = CommentDto.builder()
                .id(comment.getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .updatedAt(comment.getUpdatedAt())
                .taskId(comment.getTask().getId())
                .userId(comment.getUser().getId())
                .username(comment.getUser().getUsername())
                .build();
        
        if (comment.getParentComment() != null) {
            dto.setParentId(comment.getParentComment().getId());
        }
        
        if (includeReplies) {
            List<Comment> replies = getRepliesByParentId(comment.getId());
            List<CommentDto> replyDtos = replies.stream()
                    .map(reply -> convertToDto(reply, false)) // Don't recursively include replies of replies
                    .collect(Collectors.toList());
            dto.setReplies(replyDtos);
        } else {
            dto.setReplies(new ArrayList<>());
        }
        
        return dto;
    }
}
