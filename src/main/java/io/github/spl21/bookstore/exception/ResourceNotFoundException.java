package io.github.spl21.bookstore.exception;

public class ResourceNotFoundException extends RuntimeException{
    
    // constructor
    public ResourceNotFoundException(String message){
        super(message);
    }
}