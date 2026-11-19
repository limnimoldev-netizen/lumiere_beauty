package com.example.backend_lumiere.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestControllerAdvice                   //Handles exceptions across REST controllers
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ProductNotFoundException.class)               //@ExceptionHandler: Defines which exception a method handles

        public ResponseEntity<Map<String, String>> handleProductNotFound(
                ProductNotFoundException ex) {

            Map<String, String> error = Map.of(
                    "error", "Product not found",
                    "message", ex.getMessage()
            );

            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(error);
        }
}
