package com.taskmanager.taskmanager.model;

public enum CollaboratorRole {
    VIEWER,    // Can only view the task
    EDITOR,    // Can edit the task but not delete or share
    ADMIN      // Can edit, delete, and share the task
}
