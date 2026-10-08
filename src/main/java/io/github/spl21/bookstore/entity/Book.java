package io.github.spl21.bookstore.entity;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "books")
@Getter
@Setter
@NoArgsConstructor
public class Book {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

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

    // Fixed: Dropped premature allocation. Hibernate maps its proxies directly here on fetch commands.
    @OneToMany(mappedBy = "book")
    private List<PurchaseItem> purchaseItems;

    @OneToMany(mappedBy = "book")
    private List<CartItem> cartItems;
}
