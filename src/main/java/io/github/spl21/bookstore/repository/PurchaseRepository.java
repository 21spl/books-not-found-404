package io.github.spl21.bookstore.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.github.spl21.bookstore.entity.Purchase;
import io.github.spl21.bookstore.enums.PaymentStatus;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {

    // 1. Spring Data handles property traversal + OrderBy automatically.
    // Maps to: user -> userId, sorting by purchaseDate descending.
    List<Purchase> findByUser_IdOrderByPurchaseDateDesc(Long userId);

    // 2. Fetch joins to solve the N+1 problem for a single Purchase.
    @Query("SELECT DISTINCT p FROM Purchase p " +
           "LEFT JOIN FETCH p.items i " +
           "LEFT JOIN FETCH i.book " +
           "WHERE p.id = :id")
    Optional<Purchase> findByIdWithItems(@Param("id") Long purchaseId);

    // 3. Fetch joins to solve the N+1 problem for all Purchases.
    @Query("SELECT DISTINCT p FROM Purchase p " +
           "JOIN FETCH p.user " +
           "JOIN FETCH p.items i " +
           "JOIN FETCH i.book " +
           "ORDER BY p.purchaseDate DESC")
    List<Purchase> findAllPurchasesWithUserAndItems();




    @Query("SELECT DISTINCT p FROM Purchase p " +
           "LEFT JOIN FETCH p.items i " +
           "LEFT JOIN FETCH i.book " +
           "JOIN FETCH p.user " +
           "WHERE p.stripeSessionId = :stripeSessionId")
    Optional<Purchase> findByStripeSessionId(@Param("stripeSessionId") String stripeSessionId);


       // find all purchases for a user where payment status is paid
       @Query("""
       SELECT p
       FROM Purchase p
       WHERE p.user.id = :userId
       AND p.paymentStatus = :status
       """)
       List<Purchase> findPurchasesByUserAndStatus(
              @Param("userId") Long userId,
              @Param("status") PaymentStatus status
       );

}