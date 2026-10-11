package io.github.spl21.bookstore.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


import java.time.LocalDateTime;

@Entity
@Table(name = "inventory_reservation",
    indexes = {
        @Index(name="idx_book_expiry", columnList = "book_id, expires_at")
    },
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_user_book_reservation", columnNames = {"user_id", "book_id"})
    }
)
@Getter
@Setter
@NoArgsConstructor
@ToString(exclude = {"user", "book"})
public class InventoryReservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Min(value = 1)
    @Column(nullable = false)
    private int quantity;


    @Column(nullable = false)
    private LocalDateTime expiresAt;



    
}