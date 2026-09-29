package io.github.spl21.bookstore.controller;



import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import io.github.spl21.bookstore.entity.Book;
import io.github.spl21.bookstore.entity.Cart;
import io.github.spl21.bookstore.entity.CartItem;
import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.service.BookService;
import io.github.spl21.bookstore.service.CartService;
import jakarta.servlet.http.HttpSession;

@Controller
public class CartController {

    @Autowired 
    private BookService bookService;
    @Autowired 
    private CartService cartService;

    @GetMapping("/cart")
    public String viewCartPage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) return "redirect:/login";

        Cart cart = cartService.getCartByUser(user);
        List<CartItem> cartItems = cart != null ? cartService.getCartItems(user) : new ArrayList<>();

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
        return "cart";
    }

    @ResponseBody
    @PostMapping("/add")
    public ResponseEntity<String> addToCart(
            @RequestParam("bookId") Long bookId, // Migrated to Long
            @RequestParam("quantity") int quantity,
            HttpSession session) {
        
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body("Please login to add items to cart.");
        }

        if (quantity <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("Quantity must be at least 1.");
        }

        Book book = bookService.getBook(bookId);
        if (book == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .body("Book with ID " + bookId + " not found.");
        }

        int copies = book.getCopies();
        if (copies == 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("This book is out of stock.");
        }
        if (copies < quantity) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                                .body("Only " + copies + " copies available.");
        }

        try {
            cartService.addBookToCart(user, book, quantity);
            return ResponseEntity.ok("Book '" + book.getTitle() + "' added to cart successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                                .body("Failed to add book to cart: " + e.getMessage());
        }
    }

    @PostMapping("/cart/update")
    @ResponseBody
    public ResponseEntity<?> updateCartItemQuantity(
            @RequestParam("itemId") Long itemId, // Migrated to Long
            @RequestParam("quantity") int quantity, 
            HttpSession session) {
        
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                                .body("Please login to access the cart.");
        }
        
        List<CartItem> items = cartService.getCartItems(user);
        CartItem item = items.stream().filter(i -> i.getItemId().equals(itemId)).findFirst().orElse(null);
        if (item == null) return ResponseEntity.badRequest().body("Invalid cart item");
        
        int available = item.getBook().getCopies();

        if (quantity < 1) {
            cartService.removeCartItem(item);
            return ResponseEntity.ok("Item removed due to zero quantity");
        } else if (quantity > available) {
            return ResponseEntity.badRequest().body("Only " + available + " copies available.");
        } else {
            cartService.updateCartItemQuantity(itemId, quantity);
            return ResponseEntity.ok("Quantity updated");
        }
    }

    @ResponseBody
    @PostMapping("/cart/remove")
    public ResponseEntity<?> removeCartItem(@RequestParam("itemId") Long itemId, HttpSession session) { // Migrated to Long
        
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return ResponseEntity.badRequest().body("Please login to access the cart.");
        }
        
        List<CartItem> items = cartService.getCartItems(user);
        CartItem item = items.stream().filter(i -> i.getItemId().equals(itemId)).findFirst().orElse(null);
        if (item == null) {
            return ResponseEntity.badRequest().body("Invalid Cart Item");
        }
        
        cartService.removeCartItem(item);
        return ResponseEntity.ok("Item removed from cart");
    }
}
