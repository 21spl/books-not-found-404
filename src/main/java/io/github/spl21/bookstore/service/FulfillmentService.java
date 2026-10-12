package io.github.spl21.bookstore.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.spl21.bookstore.entity.Book;
import io.github.spl21.bookstore.entity.Purchase;
import io.github.spl21.bookstore.entity.PurchaseItem;
import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.enums.PaymentStatus;
import io.github.spl21.bookstore.exception.ResourceNotFoundException;
import io.github.spl21.bookstore.repository.InventoryReservationRepository;
import io.github.spl21.bookstore.repository.PurchaseRepository;

@Service
public class FulfillmentService {

    private final PurchaseRepository purchaseRepository;
    private final InventoryReservationRepository reservationRepository;
    private final CartService cartService;
    private final BookService bookService; // Assumes your legacy bookService has updateBook method

    public FulfillmentService(
            PurchaseRepository purchaseRepository,
            InventoryReservationRepository reservationRepository,
            CartService cartService,
            BookService bookService) {
        this.purchaseRepository = purchaseRepository;
        this.reservationRepository = reservationRepository;
        this.cartService = cartService;
        this.bookService = bookService;
    }

    /**
     * Executes the final atomic fulfillment updates under strict transaction isolation boundaries.
     */
    @Transactional
    public void fulfillOrder(String stripeSessionId) {
        // 1. Fetch the targeted pending purchase record via the indexed stripeSessionId string
        Purchase purchase = purchaseRepository.findByStripeSessionId(stripeSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found for session tracking: " + stripeSessionId));

        // Idempotency Check: Webhooks can occasionally fire multiple times for the same transaction frame.
        // If it's already processed, terminate execution cleanly to prevent duplicate stock reductions.
        if (purchase.getPaymentStatus() == PaymentStatus.PAID) {
            return;
        }

        User user = purchase.getUser();

        // 2. Loop through each snapshot item, mutate core inventory levels, and drop reservations
        for (PurchaseItem item : purchase.getItems()) {
            Book book = item.getBook();

            // Permanently modify the absolute catalog levels inside database state scope
            // Note: Hibernate automatically applies the @Version optimistic check on flush here.
            book.setCopies(book.getCopies() - item.getQuantity());
            bookService.updateBook(book);

            // Clean up and clear the temporary reservation record since the sale is finalized
            reservationRepository.deleteByBookIdAndUserId(book.getId(), user.getId());
        }

        // 3. Complete the state machine transition
        purchase.setPaymentStatus(PaymentStatus.PAID);
        purchaseRepository.save(purchase);

        // 4. Safely clear out the user's volatile cart items since they have formally bought the books
        cartService.clearCart(user);
    }
}
