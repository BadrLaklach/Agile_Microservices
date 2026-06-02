package com.agile.taskservice.exception;

public class InvalidTaskTypeException extends RuntimeException {
    public InvalidTaskTypeException(String message) {
        super(message);
    }
}
