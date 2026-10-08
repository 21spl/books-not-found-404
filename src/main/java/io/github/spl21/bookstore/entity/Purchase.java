package io.github.spl21.bookstore.entity;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "purchase")
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
