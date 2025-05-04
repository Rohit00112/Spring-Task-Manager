package com.taskmanager.taskmanager.repository;

import com.taskmanager.taskmanager.model.TemplateSubtask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TemplateSubtaskRepository extends JpaRepository<TemplateSubtask, Long> {
    List<TemplateSubtask> findByTemplateId(Long templateId);
    List<TemplateSubtask> findByTemplateIdOrderByPositionAsc(Long templateId);
}
