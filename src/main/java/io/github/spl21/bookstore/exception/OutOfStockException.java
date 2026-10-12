package io.github.spl21.bookstore.exception;

import java.util.List;

public class OutOfStockException extends RuntimeException {
    private final List<String> errors;
    public OutOfStockException(List<String> errors) {
        this.errors = errors;
    }
    public List<String> getErrors() { return errors; }
}