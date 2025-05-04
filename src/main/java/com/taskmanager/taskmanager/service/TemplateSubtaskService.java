package com.taskmanager.taskmanager.service;

import com.taskmanager.taskmanager.model.TemplateSubtask;
import com.taskmanager.taskmanager.repository.TemplateSubtaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class TemplateSubtaskService {

    @Autowired
    private TemplateSubtaskRepository subtaskRepository;

    /**
     * Create a new template subtask
     * 
     * @param subtask The subtask to create
     * @return The created subtask
     */
    @Transactional
    public TemplateSubtask createSubtask(TemplateSubtask subtask) {
        // Set position if not provided
        if (subtask.getPosition() == null) {
            List<TemplateSubtask> existingSubtasks = subtaskRepository.findByTemplateId(subtask.getTemplate().getId());
            subtask.setPosition(existingSubtasks.size() + 1);
        }
        
        return subtaskRepository.save(subtask);
    }

    /**
     * Get all subtasks for a template
     * 
     * @param templateId The ID of the template
     * @return A list of subtasks
     */
    public List<TemplateSubtask> getSubtasksByTemplateId(Long templateId) {
        return subtaskRepository.findByTemplateIdOrderByPositionAsc(templateId);
    }

    /**
     * Get a subtask by ID
     * 
     * @param subtaskId The ID of the subtask
     * @return The subtask, if found
     */
    public Optional<TemplateSubtask> getSubtaskById(Long subtaskId) {
        return subtaskRepository.findById(subtaskId);
    }

    /**
     * Update a subtask
     * 
     * @param subtask The subtask to update
     * @return The updated subtask
     */
    @Transactional
    public TemplateSubtask updateSubtask(TemplateSubtask subtask) {
        return subtaskRepository.save(subtask);
    }

    /**
     * Delete a subtask
     * 
     * @param subtaskId The ID of the subtask to delete
     */
    @Transactional
    public void deleteSubtask(Long subtaskId) {
        subtaskRepository.deleteById(subtaskId);
    }

    /**
     * Reorder subtasks
     * 
     * @param templateId The ID of the template
     * @param subtaskIds The ordered list of subtask IDs
     * @return The reordered subtasks
     */
    @Transactional
    public List<TemplateSubtask> reorderSubtasks(Long templateId, List<Long> subtaskIds) {
        List<TemplateSubtask> subtasks = subtaskRepository.findByTemplateId(templateId);
        
        for (int i = 0; i < subtaskIds.size(); i++) {
            Long subtaskId = subtaskIds.get(i);
            subtasks.stream()
                    .filter(subtask -> subtask.getId().equals(subtaskId))
                    .findFirst()
                    .ifPresent(subtask -> {
                        subtask.setPosition(i + 1);
                        subtaskRepository.save(subtask);
                    });
        }
        
        return subtaskRepository.findByTemplateIdOrderByPositionAsc(templateId);
    }
}
