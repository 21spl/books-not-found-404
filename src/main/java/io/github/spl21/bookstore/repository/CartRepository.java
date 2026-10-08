package io.github.spl21.bookstore.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import io.github.spl21.bookstore.entity.Cart;

public interface CartRepository extends JpaRepository<Cart, Long> {

    // Spring Data automatically traverses relationship: user -> userId
    Optional<Cart> findByUser_Id(Long userId);
}
