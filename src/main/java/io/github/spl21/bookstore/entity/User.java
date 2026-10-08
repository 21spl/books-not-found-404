package io.github.spl21.bookstore.entity;

import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import io.github.spl21.bookstore.enums.Role;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "users") 
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"password", "purchases", "cart"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.CUSTOMER; 


    @OneToMany(mappedBy = "user")
    private List<Purchase> purchases;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Cart cart;

    // Keeps both sides of the User <-> Cart link in sync without stack loops
    public void setCart(Cart cart) {
        this.cart = cart;
        if (cart != null && cart.getUser() != this) {
            cart.setUser(this);
        }
    }
}
