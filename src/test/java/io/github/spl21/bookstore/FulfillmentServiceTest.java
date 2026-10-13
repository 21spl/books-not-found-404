package io.github.spl21.bookstore;

import io.github.spl21.bookstore.service.BookService;
import io.github.spl21.bookstore.service.CartService;
import io.github.spl21.bookstore.service.FulfillmentService;

import io.github.spl21.bookstore.entity.Book;
import io.github.spl21.bookstore.entity.User; // Added missing User import
import io.github.spl21.bookstore.enums.*;
import io.github.spl21.bookstore.entity.Purchase;
import io.github.spl21.bookstore.entity.PurchaseItem;
import io.github.spl21.bookstore.repository.InventoryReservationRepository;
import io.github.spl21.bookstore.repository.PurchaseRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FulfillmentServiceTest {

    @Mock private PurchaseRepository purchaseRepository;
    @Mock private InventoryReservationRepository reservationRepository;
    @Mock private CartService cartService;
    @Mock private BookService bookService;

    @InjectMocks private FulfillmentService fulfillmentService;

    @Test
    public void testFulfillOrder_SuccessWorkflow() {
        String testSessionId = "cs_test_mock12345";

        // 1. Setup mock target User 
        User mockUser = new User();
        mockUser.setId(1L); // Give it a dummy ID matching your primary key mappings
        mockUser.setUsername("testShopper");
        mockUser.setEmail("d6PdM@example.com");
        mockUser.setPassword("password");
        mockUser.setName("testShopper");

        // 2. Setup mock target entities
        Book mockBook = new Book();
        mockBook.setId(10L);
        mockBook.setCopies(10);
        mockBook.setPrice(100);

        Purchase pendingPurchase = new Purchase();
        pendingPurchase.setUser(mockUser); // FIX: Bind the mock user to the purchase state context
        pendingPurchase.setPaymentStatus(PaymentStatus.PENDING);
        pendingPurchase.setStripeSessionId(testSessionId);

        PurchaseItem mockItem = new PurchaseItem();
        mockItem.setBook(mockBook);
        mockItem.setPriceAtPurchase(100);
        mockItem.setQuantity(2);
        mockItem.setPurchase(pendingPurchase);
        pendingPurchase.setItems(List.of(mockItem));

        // Define repository behaviors
        when(purchaseRepository.findByStripeSessionId(testSessionId))
                .thenReturn(Optional.of(pendingPurchase));

        // Execute targeted service method under inspection
        fulfillmentService.fulfillOrder(testSessionId);

        // Assertions: Validate that internal values transformed correctly
        assertEquals(PaymentStatus.PAID, pendingPurchase.getPaymentStatus(), "Purchase state machine should update to PAID.");
        assertEquals(8, mockBook.getCopies(), "Master inventory copies should decrement correctly (10 - 2 = 8).");

        // Verification: Ensure structural side-effect methods fired in tandem
        verify(bookService, times(1)).updateBook(mockBook);
        verify(reservationRepository, times(1)).deleteByBookIdAndUserId(any(), any());
        verify(cartService, times(1)).clearCart(any());
        verify(purchaseRepository, times(1)).save(pendingPurchase);
    }
}
