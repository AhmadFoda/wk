package com.wolterskluwer.backend.advice;

import com.wolterskluwer.backend.exception.ForbiddenOperationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ExceptionHandlerAdvice {
    @ExceptionHandler(ForbiddenOperationException.class)
    public ResponseEntity<?> handleForbiddenRequests(ForbiddenOperationException forbiddenOperationException) {
        return ResponseEntity.status(403).body(forbiddenOperationException.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<?> handleIllegalArgumentException(IllegalArgumentException forbiddenOperationException) {
        return ResponseEntity.status(400).body(forbiddenOperationException.getMessage());
    }
}
