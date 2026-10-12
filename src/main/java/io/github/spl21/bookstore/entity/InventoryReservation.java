package io.github.spl21.bookstore.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Table(
    name = "inventory_reservations",
    indexes = {
        @Index(name = "idx_book_expiry", columnList = "book_id, expires_at")
    }
)
public class InventoryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "quantity", nullable = false)
    private int quantity;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expires_at;

    // Default Constructor, Getters, and Setters
    public InventoryReservation() {}

    public InventoryReservation(Book book, User user, int quantity, int minutesToHold) {
        this.book = book;
        this.user = user;
        this.quantity = quantity;
        this.expires_at = LocalDateTime.now().plusMinutes(minutesToHold);
    }

    public Long getId() { return id; }
    public Book getBook() { return book; }
    public User getUser() { return user; }
    public int getQuantity() { return quantity; }
    public LocalDateTime getExpiresAt() { return expires_at; }
    
    public void setExpiresAt(LocalDateTime expiresAt) { this.expires_at = expiresAt; }
}