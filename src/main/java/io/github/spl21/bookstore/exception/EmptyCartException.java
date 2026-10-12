package io.github.spl21.bookstore.exception;

public class EmptyCartException extends RuntimeException {

    public EmptyCartException(String message) { 
        super(message); 
    }
}
