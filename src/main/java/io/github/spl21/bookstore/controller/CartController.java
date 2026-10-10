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
import io.github.spl21.bookstore.repository.UserRepository;
import io.github.spl21.bookstore.service.BookService;
import io.github.spl21.bookstore.service.CartService;

@Controller
@RequestMapping("/cart")
public class CartController {

    private final BookService bookService;
    private final CartService cartService;
    private final UserRepository userRepository;

    public CartController(
            BookService bookService,
            CartService cartService,
            UserRepository userRepository) {

        this.bookService = bookService;
        this.cartService = cartService;
        this.userRepository = userRepository;
    }

    // Retrieve the currently authenticated user's database entity.
    private Optional<User> getCurrentUser(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return Optional.empty();
        }

        return userRepository.findByUsername(authentication.getName());
    }
    // View cart
    @GetMapping
    public String viewCartPage(Authentication authentication, Model model) {

        Optional<User> userOptional = getCurrentUser(authentication);

        if (userOptional.isEmpty()) {
            return "redirect:/auth/login";
        }

        User user = userOptional.get();

        Cart cart = cartService.getCartByUser(user);

        List<CartItem> cartItems = cart != null
                ? cartService.getCartItems(user)
                : new ArrayList<>();

        double totalAmount = 0;
        boolean hasOutOfStock = false;

        for (CartItem item : cartItems) {
            totalAmount += item.getQuantity() * item.getBook().getPrice();

            if (item.getQuantity() > item.getBook().getCopies()) {
                hasOutOfStock = true;
            }
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        model.addAttribute("hasOutOfStock", hasOutOfStock);

        return "customer/cart";
    }

    // Add a book to the cart
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

        Book book = bookService.getBook(bookId);

        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Book with ID " + bookId + " not found.");
        }

        int copies = book.getCopies();

        if (copies <= 0) {
            return ResponseEntity.badRequest()
                    .body("This book is out of stock.");
        }

        if (quantity > copies) {
            return ResponseEntity.badRequest()
                    .body("Only " + copies + " copies available.");
        }

        User user = userOptional.get();

        try {
            cartService.addBookToCart(user, book, quantity);

            return ResponseEntity.ok(
                    "Book '" + book.getTitle() + "' added to cart successfully!");

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add book to cart.");
        }
    }

    // Update cart item quantity
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
            return ResponseEntity.badRequest()
                    .body("Invalid cart item.");
        }

        if (quantity < 1) {
            cartService.removeCartItem(item);

            return ResponseEntity.ok("Item removed due to zero quantity.");
        }

        int available = item.getBook().getCopies();

        if (quantity > available) {
            return ResponseEntity.badRequest()
                    .body("Only " + available + " copies available.");
        }

        cartService.updateCartItemQuantity(itemId, quantity);

        return ResponseEntity.ok("Quantity updated.");
    }

    // Remove cart item
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
            return ResponseEntity.badRequest()
                    .body("Invalid cart item.");
        }

        cartService.removeCartItem(item);

        return ResponseEntity.ok("Item removed from cart.");
    }
}