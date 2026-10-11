package io.github.spl21.bookstore.exception;

public class PaymentGatewayException extends RuntimeException {
    
    // Default constructor
    public PaymentGatewayException() {
        super();
    }

    // Constructor that accepts a custom error message
    public PaymentGatewayException(String message) {
        super(message);
    }

    // Constructor that accepts a custom message and the root cause (e.g., StripeException)
    public PaymentGatewayException(String message, Throwable cause) {
        super(message, cause);
    }

    // Constructor that accepts the root cause
    public PaymentGatewayException(Throwable cause) {
        super(cause);
    }
}
