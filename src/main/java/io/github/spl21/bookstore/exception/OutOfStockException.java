package io.github.spl21.bookstore.exception;


public class OutOfStockException extends RuntimeException {

    public OutOfStockException() {
        super("This book is out of stock.");
    }
}