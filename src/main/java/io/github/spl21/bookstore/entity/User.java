package io.github.spl21.bookstore.entity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

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

    @NotBlank(message = "username is required")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message = "Full name is required")
    @Column(nullable = false)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email")
    @Column(unique = true, nullable = false)
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters long")
    private String password;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Enumerated(EnumType.STRING)
    private Set<Role> roles = new HashSet<>(Set.of(Role.CUSTOMER));

    // mappedBy field name "user", but FK column name is "user_id"
    @OneToMany(mappedBy = "user") // FK in purchase table 
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
