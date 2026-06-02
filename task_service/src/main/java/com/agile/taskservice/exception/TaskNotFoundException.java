package com.agile.taskservice.exception;

import java.util.UUID;

public class TaskNotFoundException extends RuntimeException {
    public TaskNotFoundException(UUID id) {
        super("Task with ID " + id + " not found");
    }
}
