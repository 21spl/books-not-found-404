package io.github.spl21.bookstore.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.Getter;

@Entity
@Table(
    name = "inventory_reservations",
    indexes = {
        @Index(name = "idx_book_expiry", columnList = "book_id, expires_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_book_reservation", columnNames = {"user_id", "book_id"})
    }
)
@Getter
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
    
    @Column(name = "total_value", nullable = false)
    private double totalValue;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt; // Changed to lowerCamelCase to match Spring Data naming defaults
    
    private static final int MINUTES_TO_HOLD = 10;

    public InventoryReservation() {}

    public InventoryReservation(Book book, User user, int quantity) {
        this.book = book;
        this.user = user;
        this.quantity = quantity;
        this.expiresAt = LocalDateTime.now().plusMinutes(MINUTES_TO_HOLD);
        this.totalValue = calculateTotalValue();
    }

    
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    public void setTotalValue(double totalValue) { this.totalValue = totalValue; }
    
    public double calculateTotalValue() {
        double bookPrice = this.book.getPrice();
        int qty = this.quantity;
        return (double) (bookPrice * qty);
    }
}
