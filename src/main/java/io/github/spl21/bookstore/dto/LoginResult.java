package io.github.spl21.bookstore.dto;

import io.github.spl21.bookstore.entity.User;
import lombok.Getter;
import lombok.Setter;
import io.github.spl21.bookstore.enums.Role;

/**
 * Data Transfer Object (DTO) wrapping the authentication result.
 * Kept isolated in the dto package to keep service layers clean.
 */
@Getter
@Setter
public class LoginResult {
    
    private boolean success;
    private String message;
    private User user;
    
    // Constructor
    public LoginResult(boolean success, String message, User user) {
        this.success = success;
        this.message = message;
        this.user = user;
    }
    
    /**
     * Checks if the authenticated user has an administrator role safely.
     * Prevents NullPointerException if the user object is null.
     */
    public boolean isAdmin() {
        return user != null && user.getRole()==Role.ADMIN;
    }
}
 