package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByTaskIdOrderByCreatedAtDesc(Long taskId);
    List<Comment> findByTaskIdAndParentCommentIsNullOrderByCreatedAtDesc(Long taskId);
    List<Comment> findByParentCommentIdOrderByCreatedAtAsc(Long parentId);
}
