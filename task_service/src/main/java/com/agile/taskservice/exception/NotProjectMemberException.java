package com.agile.taskservice.exception;

public class NotProjectMemberException extends RuntimeException {
    public NotProjectMemberException() {
        super("You are not a member of this project");
    }
}
