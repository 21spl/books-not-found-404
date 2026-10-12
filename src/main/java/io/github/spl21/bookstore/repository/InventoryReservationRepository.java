package io.github.spl21.bookstore.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.github.spl21.bookstore.entity.InventoryReservation;

public interface InventoryReservationRepository extends JpaRepository<InventoryReservation, Long> {
    
    @Query("SELECT COALESCE(SUM(r.quantity), 0) FROM InventoryReservation r " +
            "WHERE r.book.id = :bookId AND r.expiresAt > :now")
    int getActiveReservedQuantity(@Param("bookId") Long bookId, @Param("now") LocalDateTime now);

    @Modifying 
    @Query("DELETE FROM InventoryReservation r WHERE r.expiresAt < :now")
    void deleteExpiredReservations(@Param("now") LocalDateTime now);

    Optional<InventoryReservation> findByBookIdAndUserIdAndExpiresAtAfter(Long bookId, Long userId, LocalDateTime now);

    List<InventoryReservation> findByUserIdAndExpiresAtAfter(Long userId, LocalDateTime now);
    
    void deleteByBookIdAndUserId(Long bookId, Long userId);
}
