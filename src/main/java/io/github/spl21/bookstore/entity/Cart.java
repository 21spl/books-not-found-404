package io.github.spl21.bookstore.entity;

import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "cart")
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"user", "cartItems"})
public class Cart {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Fixed: Left uninitialized so Hibernate proxies can bind efficiently
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> cartItems;

    private double totalAmount;

    // keep both sides of the relationship in sync
    public void addItem(CartItem item) {
        if (this.cartItems == null) {
            this.cartItems = new java.util.ArrayList<>();
        }
        cartItems.add(item);
        item.setCart(this);
    }

    public void removeItem(CartItem item) {
        if (cartItems != null) {
            cartItems.remove(item);
            item.setCart(null);
        }
    }

    // Fixed loop boundary: safely maps user without driving infinite mutual assignment chain
    public void setUser(User user) {
        this.user = user;
        if (user != null && user.getCart() != this) {
            user.setCart(this);
        }
    }
}
