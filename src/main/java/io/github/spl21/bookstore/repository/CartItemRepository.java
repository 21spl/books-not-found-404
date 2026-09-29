package io.github.spl21.bookstore.repository;

import io.github.spl21.bookstore.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 1. Spring Data automatically traverses relationship: cart -> cartId
    List<CartItem> findByCart_CartId(Long cartId);

    // 2. Multi-conditional query traversal: book -> bookId AND cart -> cartId
    Optional<CartItem> findByBook_BookIdAndCart_CartId(Long bookId, Long cartId);
}
