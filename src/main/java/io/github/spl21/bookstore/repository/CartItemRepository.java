package io.github.spl21.bookstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.spl21.bookstore.entity.CartItem;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // 1. Spring Data automatically traverses relationship: cart -> cartId
    List<CartItem> findByCart_Id(Long cartId);

    // 2. Multi-conditional query traversal: book -> bookId AND cart -> cartId
    Optional<CartItem> findByBook_IdAndCart_Id(Long bookId, Long cartId);
}
