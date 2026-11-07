package com.quota.backend.exception;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice 
public class GlobalExceptionHandler {
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String,Object>> handleArgMismatch(MethodArgumentTypeMismatchException ex){
        Map<String, Object> errorDetails = new HashMap<>();

        if (ex.getRequiredType() != null && ex.getRequiredType().equals(UUID.class)) {
            errorDetails.put("error", "Bad Request");
            errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
            errorDetails.put("message", String.format("The parameter '%s' must be a valid UUID format (e.g., 123e4567-e89b-12d3-a456-426614174000).", ex.getName()));
            errorDetails.put("invalidValue", ex.getValue());
            
            return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST);
        }

        // Fallback message for other type mismatches (e.g., passing text instead of an Integer)
        errorDetails.put("error", "Bad Request");
        errorDetails.put("status", HttpStatus.BAD_REQUEST.value());
        errorDetails.put("message", "Invalid parameter type provided.");

        return new ResponseEntity<>(errorDetails, HttpStatus.BAD_REQUEST );
    }    
}
