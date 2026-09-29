package io.github.spl21.bookstore.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long bookId;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author name is required")
    private String author;

    @Column(length = 512)
    private String description;

    private String genre;

    @Min(value = 0, message = "Copies cannot be negative")
    private int copies;

    @Positive(message = "Price must be greater than 0")
    private double price;

    @OneToMany(mappedBy = "book")
    private List<PurchaseItem> purchaseItems = new ArrayList<>();

    @OneToMany(mappedBy = "book")
    private List<CartItem> cartItems = new ArrayList<>();
}