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
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FulfillmentService {

    private final PurchaseRepository purchaseRepository;
    private final InventoryReservationRepository reservationRepository;
    private final CartService cartService;
    private final BookService bookService; // Assumes your legacy bookService manages entity flushes

    /**
     * Executes the final atomic fulfillment updates under strict transaction boundaries
     * once Stripe fires a successful payment confirmation webhook.
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

    /**
     * Safely cancels a pending order when Stripe notifies us of an expiration or failure.
     * Keeps history row intact for business analytics, but frees locked stock instantly.
     */
    @Transactional
    public void cancelOrder(String stripeSessionId) {
        Purchase purchase = purchaseRepository.findByStripeSessionId(stripeSessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Purchase not found for session: " + stripeSessionId));

        // Only cancel if the state is currently PENDING
        if (purchase.getPaymentStatus() != PaymentStatus.PENDING) {
            return;
        }

        // 1. Transition the state machine to FAILED instead of deleting the tracking row
        purchase.setPaymentStatus(PaymentStatus.FAILED);
        purchaseRepository.save(purchase);

        // 2. Instantly release the inventory reservations so other shoppers can buy the books
        User user = purchase.getUser();
        for (PurchaseItem item : purchase.getItems()) {
            reservationRepository.deleteByBookIdAndUserId(item.getBook().getId(), user.getId());
        }

        // User's Cart remains untouched! Because cartService.clearCart(user) is 
        // deliberately omitted here, the user can easily review their cart and retry payment.
    }
}
