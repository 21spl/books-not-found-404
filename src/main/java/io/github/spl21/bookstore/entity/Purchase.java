package io.github.spl21.bookstore.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;

import io.github.spl21.bookstore.enums.PaymentStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "purchase",
    indexes = {
        @Index(name = "idx_stripe_session", columnList = "stripe_session_id", unique = true)
    }
)
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"user", "items"})
public class Purchase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDateTime purchaseDate;

    // many purchases can belong to one user
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Fixed: Left uninitialized so Hibernate proxies can bind efficiently
    @OneToMany(mappedBy = "purchase", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PurchaseItem> items;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotBlank(message = "Address is required")
    @Column(length = 500)
    private String address;

    private double totalAmount;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false, length = 30)
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    @Column(name = "stripe_session_id", unique = true)
    private String stripeSessionId;


    public void addItem(PurchaseItem item) {
        if (this.items == null) {
            this.items = new java.util.ArrayList<>();
        }
        items.add(item);
        item.setPurchase(this);
    }

    @PrePersist
    void onCreate() {
        if (purchaseDate == null) {
            purchaseDate = LocalDateTime.now();
        }
    }
}
