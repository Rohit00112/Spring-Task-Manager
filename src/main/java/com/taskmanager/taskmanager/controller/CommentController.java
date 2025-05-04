package com.taskmanager.taskmanager.controller;

import com.taskmanager.taskmanager.dto.CommentDto;
import com.taskmanager.taskmanager.model.Comment;
import com.taskmanager.taskmanager.model.Task;
import com.taskmanager.taskmanager.model.User;
import com.taskmanager.taskmanager.service.CommentService;
import com.taskmanager.taskmanager.service.TaskService;
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

@RestController
@RequestMapping("/api/tasks/{taskId}/comments")
public class CommentController {

    @Autowired
    private CommentService commentService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<?> createComment(@PathVariable Long taskId, @RequestBody CommentDto commentDto) {
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
        User user = userOptional.get();
        
        Comment comment = Comment.builder()
                .content(commentDto.getContent())
                .task(task)
                .user(user)
                .build();
        
        // If this is a reply, set the parent comment
        if (commentDto.getParentId() != null) {
            Optional<Comment> parentCommentOptional = commentService.getCommentById(commentDto.getParentId());
            if (parentCommentOptional.isPresent()) {
                Comment parentComment = parentCommentOptional.get();
                if (!parentComment.getTask().getId().equals(taskId)) {
                    return ResponseEntity.badRequest().body(Map.of("error", "Parent comment does not belong to the specified task"));
                }
                comment.setParentComment(parentComment);
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Parent comment not found"));
            }
        }
        
        Comment savedComment = commentService.createComment(comment);
        CommentDto responseDto = commentService.convertToDto(savedComment, false);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<?> getComments(@PathVariable Long taskId) {
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
        
        List<CommentDto> comments = commentService.getCommentsWithReplies(taskId);
        return ResponseEntity.ok(comments);
    }

    @GetMapping("/{commentId}")
    public ResponseEntity<?> getComment(@PathVariable Long taskId, @PathVariable Long commentId) {
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
        
        Optional<Comment> commentOptional = commentService.getCommentById(commentId);
        if (commentOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Comment comment = commentOptional.get();
        if (!comment.getTask().getId().equals(taskId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Comment does not belong to the specified task"));
        }
        
        CommentDto commentDto = commentService.convertToDto(comment, true);
        return ResponseEntity.ok(commentDto);
    }

    @PutMapping("/{commentId}")
    public ResponseEntity<?> updateComment(@PathVariable Long taskId, @PathVariable Long commentId, @RequestBody CommentDto commentDto) {
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
        
        Optional<Comment> commentOptional = commentService.getCommentById(commentId);
        if (commentOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Comment comment = commentOptional.get();
        if (!comment.getTask().getId().equals(taskId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Comment does not belong to the specified task"));
        }
        
        // Only the comment author can update it
        if (!comment.getUser().getId().equals(userOptional.get().getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to update this comment"));
        }
        
        comment.setContent(commentDto.getContent());
        Comment updatedComment = commentService.updateComment(comment);
        
        CommentDto responseDto = commentService.convertToDto(updatedComment, true);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{commentId}")
    public ResponseEntity<?> deleteComment(@PathVariable Long taskId, @PathVariable Long commentId) {
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
        
        Optional<Comment> commentOptional = commentService.getCommentById(commentId);
        if (commentOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        Comment comment = commentOptional.get();
        if (!comment.getTask().getId().equals(taskId)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Comment does not belong to the specified task"));
        }
        
        // Only the comment author or task owner can delete it
        User currentUser = userOptional.get();
        Task task = taskOptional.get();
        if (!comment.getUser().getId().equals(currentUser.getId()) && !task.getUser().getId().equals(currentUser.getId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "You don't have permission to delete this comment"));
        }
        
        commentService.deleteComment(commentId);
        return ResponseEntity.noContent().build();
    }
}
