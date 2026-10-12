package io.github.spl21.bookstore.exception;

public class NotEnoughStockException extends RuntimeException {
    public NotEnoughStockException(int requested, int available) {
        super("Not enough stock. Requested: " + requested + ", Available: " + available);
    }
}
