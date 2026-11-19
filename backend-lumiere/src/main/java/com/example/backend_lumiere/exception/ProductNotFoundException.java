package com.example.backend_lumiere.exception;

public class  ProductNotFoundException extends RuntimeException {
    
    public ProductNotFoundException(String message) {
        super(message);
    }
    
}
