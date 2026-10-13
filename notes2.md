Concurrency Test: Preventing Overage Sales (The Last Copy Race Condition)

```java

package io.github.spl21.bookstore.service;

import io.github.spl21.bookstore.entity.Book;
import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.repository.BookRepository;
import io.github.spl21.bookstore.repository.InventoryReservationRepository;
import io.github.spl21.bookstore.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("dev") // Uses your test database profile configurations
public class InventoryConcurrencyTest {

    @Autowired private InventoryReservationService reservationService;
    @Autowired private BookRepository bookRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private InventoryReservationRepository reservationRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Long sharedBookId;
    private Long userAId;
    private Long userBId;

    @BeforeEach
    public void setup() {
        reservationRepository.deleteExpiredReservations(LocalDateTime.now());
        
        // 1. Setup a single target book with exactly 1 copy left in stock
        Book book = new Book();
        book.setTitle("The Flash Sale Book");
        book.setAuthor("Celebrity Author");
        book.setPrice(500);
        book.setCopies(1); // Only 1 copy available!
        book = bookRepository.saveAndFlush(book);
        this.sharedBookId = book.getId();

        // 2. Setup two distinct test shoppers
        User userA = new User();
        userA.setUsername("userA");
        userA.setEmail("a@example.com");
        userA.setPassword(passwordEncoder.encode("passwordA"));
        userA = userRepository.saveAndFlush(userA);
        this.userAId = userA.getId();

        User userB = new User();
        userB.setUsername("userB");
        userB.setEmail("b@example.com");
        userB.setPassword(passwordEncoder.encode("passwordB"));
        userB = userRepository.saveAndFlush(userB);
        this.userBId = userB.getId();
    }

    @Test
    public void testConcurrentReservations_OnlyOneShouldSucceed() throws InterruptedException {

        // Simulate two concurrent checkouts
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1); // Sync point to release threads together
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        // Submit Thread #1 (User A)
        executor.submit(() -> {
            try {
                latch.await(); // Hold execution until released
                boolean status = reservationService.reserveInventory(sharedBookId, userAId, 1);
                if (status) successCount.incrementAndGet(); else failureCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            }
        });

        // Submit Thread #2 (User B)
        executor.submit(() -> {
            try {
                latch.await(); // Hold execution until released
                boolean status = reservationService.reserveInventory(sharedBookId, userBId, 1);
                if (status) successCount.incrementAndGet(); else failureCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            }
        });

        // Fire threads simultaneously!
        latch.countDown();
        
        // Wait for workers to complete execution blocks
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(50);
        }

        // Assertions: Out of two parallel checkouts, exactly 1 must pass and 1 must fail
        assertEquals(1, successCount.get(), "Exactly one user should have secured the last reservation.");
        assertEquals(1, failureCount.get(), "The second concurrent user must be rejected cleanly.");
        
        // Double check database state maps to exactly 1 total active reservation row
        long totalReservations = reservationRepository.count();
        assertEquals(1, totalReservations, "Database reservation table should contain only one active record.");
    }
}


```