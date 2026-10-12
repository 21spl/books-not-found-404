package io.github.spl21.bookstore.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.spl21.bookstore.dto.CheckoutSummaryDTO;
import io.github.spl21.bookstore.dto.OrderFormDto;
import io.github.spl21.bookstore.entity.Cart;
import io.github.spl21.bookstore.entity.CartItem;
import io.github.spl21.bookstore.entity.InventoryReservation;
import io.github.spl21.bookstore.entity.Purchase;
import io.github.spl21.bookstore.entity.PurchaseItem;
import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.enums.PaymentStatus;
import io.github.spl21.bookstore.exception.EmptyCartException;
import io.github.spl21.bookstore.exception.NotEnoughStockException;
import io.github.spl21.bookstore.exception.UserNotFoundException;
import io.github.spl21.bookstore.repository.PurchaseRepository;
import io.github.spl21.bookstore.repository.UserRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CheckoutService {

    private final UserRepository userRepository;
    private final CartService cartService;
    private final InventoryReservationService reservationService;
    private final StripeService stripeService;
    private final PurchaseRepository purchaseRepository;
    private final BookService bookService;

    @Transactional(readOnly = true)
    public CheckoutSummaryDTO getCheckoutSummary(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User session invalid"));

        Cart cart = cartService.getCartByUser(user);
        if (cart == null) {
            throw new EmptyCartException("User does not have an open cart");
        }

        List<CartItem> cartItems = cartService.getCartItems(user);
        if (cartItems.isEmpty()) {
            throw new EmptyCartException("Cart contains no products");
        }
        
        List<String> stockWarnings = new ArrayList<>();
        double totalAmount = 0;

        for (CartItem item : cartItems) {
            Long reqBookId = item.getBook().getId();
            int reqQuantity = item.getQuantity();
            int availableStock = bookService.getActualStock(reqBookId);
            
            boolean secured = reservationService.reserveInventory(reqBookId, user.getId(), reqQuantity);
            if (!secured) {
                throw new NotEnoughStockException(reqQuantity, availableStock);
            }

            totalAmount += (item.getQuantity() * item.getBook().getPrice());
            if (availableStock < 5) {
                stockWarnings.add(item.getBook().getTitle() + " has critically low stock!");
            }
        }
        
        return new CheckoutSummaryDTO(cartItems, totalAmount, stockWarnings);
    }

    @Transactional
    public String initiateCheckoutPayment(String username, OrderFormDto orderForm) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User session invalid"));

        List<InventoryReservation> checkoutItems = reservationService.getAllReservationsByUsername(username);
        if (checkoutItems.isEmpty()) {
            throw new EmptyCartException("No active allocations locked for current checkout session");
        }
        
        // 1. Build and persist the PENDING Purchase ledger row right away
        Purchase purchase = new Purchase();
        purchase.setUser(user);
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setAddress(orderForm.getAddress());
        purchase.setPhone(orderForm.getPhone());
        purchase.setPaymentStatus(PaymentStatus.PENDING); 

        List<PurchaseItem> purchaseItems = new ArrayList<>();
        double totalAmount = 0;

        for (InventoryReservation item : checkoutItems) {
            totalAmount += item.getTotalValue();

            PurchaseItem purchaseItem = new PurchaseItem();
            purchaseItem.setBook(item.getBook());
            purchaseItem.setQuantity(item.getQuantity());
            purchaseItem.setPurchase(purchase);
            purchaseItem.setPriceAtPurchase(item.getBook().getPrice()); // Price snapshot protection
            purchaseItems.add(purchaseItem);
        }

        purchase.setTotalAmount(totalAmount);
        purchase.setItems(purchaseItems);
        
        purchaseRepository.save(purchase); // Save to generate database reference frame

        // 2. Generate Stripe Hosted Terminal Links using the Purchase entity
        String stripeUrl = stripeService.createCheckoutSession(purchase);
        String stripeSessionId = stripeService.extractSessionIdFromUrl(stripeUrl);

        // 3. Bind the unique Stripe session ID token directly to the pending row
        purchase.setStripeSessionId(stripeSessionId);
        purchaseRepository.save(purchase);

        return stripeUrl; // Return raw redirection string link back to the controller
    }
}
