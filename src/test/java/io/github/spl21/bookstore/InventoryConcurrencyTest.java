package io.github.spl21.bookstore;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import io.github.spl21.bookstore.entity.Book;
import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.repository.BookRepository;
import io.github.spl21.bookstore.repository.CartItemRepository; // Added injection mapping
import io.github.spl21.bookstore.repository.CartRepository;     // Added injection mapping
import io.github.spl21.bookstore.repository.InventoryReservationRepository;
import io.github.spl21.bookstore.repository.PurchaseRepository; // Added injection mapping
import io.github.spl21.bookstore.repository.UserRepository;
import io.github.spl21.bookstore.service.InventoryReservationService;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("dev") 
public class InventoryConcurrencyTest {

    @Autowired private InventoryReservationService reservationService;
    @Autowired private BookRepository bookRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private InventoryReservationRepository reservationRepository;
    @Autowired private PurchaseRepository purchaseRepository;
    @Autowired private CartItemRepository cartItemRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Long sharedBookId;
    private Long userAId;
    private Long userBId;

    @BeforeEach
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void setup() {
        // FIX: Clean the tables in strict structural order (Children tables wiped out first)
        clearDatabaseSafely();
        
        // 1. Setup a single target book with exactly 1 copy left in stock
        Book book = new Book();
        book.setTitle("The Flash Sale Book");
        book.setAuthor("Celebrity Author");
        book.setPrice(500);
        book.setCopies(1);
        book = bookRepository.saveAndFlush(book);
        this.sharedBookId = book.getId();

        // 2. Setup two distinct test shoppers
        User userA = new User();
        userA.setName("userA");
        userA.setUsername("userA");
        userA.setEmail("a@example.com");
        userA.setPassword(passwordEncoder.encode("passwordA"));
        userA = userRepository.saveAndFlush(userA);
        this.userAId = userA.getId();

        User userB = new User();
        userB.setName("userB");
        userB.setUsername("userB");
        userB.setEmail("b@example.com");
        userB.setPassword(passwordEncoder.encode("passwordB"));
        userB = userRepository.saveAndFlush(userB);
        this.userBId = userB.getId();
    }

    @AfterEach
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void tearDown() {
        // FIX: Clear records cleanly after execution block terminates
        clearDatabaseSafely();
    }

    /**
     * Private helper utility that untangles foreign key constraints sequentially.
     */
    private void clearDatabaseSafely() {
        // Level 1: Clean dynamic operational child items first
        reservationRepository.deleteAll();
        
        // Wipe historical cart item layouts
        cartItemRepository.deleteAll();
        cartRepository.deleteAll();
        
        // Wipe purchase and order snapshot rows mapping to child items
        purchaseRepository.deleteAll(); // Wiping this removes dependencies referencing users table!

        // Level 2: Safely wipe out master core parent rows since zero constraints remain active
        userRepository.deleteAll();
        bookRepository.deleteAll();
    }

    @Test
    public void testConcurrentReservations_OnlyOneShouldSucceed() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);
        
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failureCount = new AtomicInteger(0);

        executor.submit(() -> {
            try {
                latch.await();
                boolean status = reservationService.reserveInventory(sharedBookId, userAId, 1);
                if (status) successCount.incrementAndGet(); else failureCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            }
        });

        executor.submit(() -> {
            try {
                latch.await();
                boolean status = reservationService.reserveInventory(sharedBookId, userBId, 1);
                if (status) successCount.incrementAndGet(); else failureCount.incrementAndGet();
            } catch (Exception e) {
                failureCount.incrementAndGet();
            }
        });

        latch.countDown();
        
        executor.shutdown();
        while (!executor.isTerminated()) {
            Thread.sleep(50);
        }

        assertEquals(1, successCount.get(), "Exactly one user should have secured the last reservation.");
        assertEquals(1, failureCount.get(), "The second concurrent user must be rejected cleanly.");
        
        long totalReservations = reservationRepository.count();
        assertEquals(1, totalReservations, "Database reservation table should contain only one active record.");
    }
}
