package io.github.spl21.bookstore.repository;

import io.github.spl21.bookstore.entity.Cart;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // Spring Data automatically traverses relationship: user -> userId
    Optional<Cart> findByUser_UserId(Long userId);
}
