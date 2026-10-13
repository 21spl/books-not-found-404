package io.github.spl21.bookstore.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.github.spl21.bookstore.entity.Book;
import io.github.spl21.bookstore.entity.Cart;
import io.github.spl21.bookstore.entity.CartItem;
import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.exception.NotEnoughStockException;
import io.github.spl21.bookstore.exception.OutOfStockException;
import io.github.spl21.bookstore.repository.UserRepository;
import io.github.spl21.bookstore.service.BookService;
import io.github.spl21.bookstore.service.CartService;
import lombok.RequiredArgsConstructor;

@Controller 
@RequiredArgsConstructor
@RequestMapping("/cart")
public class CartController {

    private final BookService bookService;
    private final CartService cartService;
    private final UserRepository userRepository;

    //===================================== Retrieve current User =============================================
    private Optional<User> getCurrentUser(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return Optional.empty();
        }
        return userRepository.findByUsername(authentication.getName());
    }

    //============================================== View cart ===============================================
    @GetMapping
    public String viewCartPage(Authentication authentication, Model model) {
        Optional<User> userOptional = getCurrentUser(authentication);
        if (userOptional.isEmpty()) {
            return "redirect:/auth/login";
        }

        User user = userOptional.get();
        Cart cart = cartService.getCartByUser(user);

        // This call automatically injects transient @Transient stock warnings into matching line items
        List<CartItem> cartItems = cart != null
                ? cartService.getCartItems(user)
                : new ArrayList<>();

        double totalAmount = 0;
        boolean hasOutOfStock = false;

        for (CartItem item : cartItems) {
            totalAmount += item.getQuantity() * item.getBook().getPrice();

            // FIX 2: Compute against real-time available stock instead of raw un-isolated copies counts
            int trueAvailableStock = bookService.getActualStock(item.getBook().getId());
            if (item.getQuantity() > trueAvailableStock) {
                hasOutOfStock = true;
            }
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("hasOutOfStock", hasOutOfStock);

        return "customer/cart";
    }

    //================================ Add a book to the cart =========================================
    @ResponseBody
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(
            @RequestParam("bookId") Long bookId,
            @RequestParam("quantity") int quantity,
            Authentication authentication) {

        Optional<User> userOptional = getCurrentUser(authentication);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please login to add items to cart.");
        }

        if (quantity <= 0) {
            return ResponseEntity.badRequest()
                    .body("Quantity must be at least 1.");
        }

        // Assumes your legacy bookService utilizes findById mapping models
        Book book = bookService.getBook(bookId); 
        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Book with ID " + bookId + " not found.");
        }

        User user = userOptional.get();
        try {
            cartService.addBookToCart(user, book, quantity);
            return ResponseEntity.ok("Book '" + book.getTitle() + "' added to cart successfully!");

        } catch (OutOfStockException | NotEnoughStockException | IllegalArgumentException e) {
            // FIX 3: Intercept specific custom business exceptions to deliver informative error responses
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred while modifying your cart selections.");
        }
    }

    //======================== Update cart item quantity===================
    @ResponseBody
    @PostMapping("/update")
    public ResponseEntity<String> updateCartItemQuantity(
            @RequestParam("itemId") Long itemId,
            @RequestParam("quantity") int quantity,
            Authentication authentication) {

        Optional<User> userOptional = getCurrentUser(authentication);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please login to access the cart.");
        }

        User user = userOptional.get();
        List<CartItem> items = cartService.getCartItems(user);

        CartItem item = items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElse(null);

        if (item == null) {
            return ResponseEntity.badRequest().body("Invalid cart item.");
        }

        if (quantity < 1) {
            cartService.removeCartItem(item);
            return ResponseEntity.ok("Item removed due to zero quantity.");
        }

        try {
            cartService.updateCartItemQuantity(itemId, quantity);
            return ResponseEntity.ok("Quantity updated successfully.");
        } catch (OutOfStockException | NotEnoughStockException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to update cart quantity thresholds.");
        }
    }

    //=========================================== Remove cart item ===========================================
    @ResponseBody
    @PostMapping("/remove")
    public ResponseEntity<String> removeCartItem(
            @RequestParam("itemId") Long itemId,
            Authentication authentication) {

        Optional<User> userOptional = getCurrentUser(authentication);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Please login to access the cart.");
        }

        User user = userOptional.get();
        List<CartItem> items = cartService.getCartItems(user);

        CartItem item = items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElse(null);

        if (item == null) {
            return ResponseEntity.badRequest().body("Invalid cart item reference requested.");
        }

        cartService.removeCartItem(item);
        return ResponseEntity.ok("Item removed from cart.");
    }
}

