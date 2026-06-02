package com.agile.taskservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskNotFoundException.class)
    public ProblemDetail handleNotFound(RuntimeException ex, HttpServletRequest req) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
        pb.setTitle("Task not found");
        pb.setInstance(java.net.URI.create(req.getRequestURI()));
        return pb;
    }

    @ExceptionHandler({AccessDeniedException.class, NotProjectMemberException.class})
    public ProblemDetail handleAccessDenied(RuntimeException ex, HttpServletRequest req) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.FORBIDDEN, ex.getMessage());
        pb.setTitle("Access denied");
        pb.setInstance(java.net.URI.create(req.getRequestURI()));
        return pb;
    }

    @ExceptionHandler({InvalidTaskTypeException.class, InvalidQueryParameterException.class})
    public ProblemDetail handleBadRequest(RuntimeException ex, HttpServletRequest req) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
        pb.setTitle("Invalid request");
        pb.setInstance(java.net.URI.create(req.getRequestURI()));
        return pb;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Constraint violation");
        pb.setTitle("Constraint violation");
        pb.setInstance(java.net.URI.create(req.getRequestURI()));
        return pb;
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        ProblemDetail pb = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Invalid parameter format");
        pb.setTitle("Invalid parameter format");
        pb.setInstance(java.net.URI.create(req.getRequestURI()));
        return pb;
    }
}
