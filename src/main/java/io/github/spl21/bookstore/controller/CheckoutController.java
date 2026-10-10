package io.github.spl21.bookstore.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.github.spl21.bookstore.entity.Book;
import io.github.spl21.bookstore.entity.Cart;
import io.github.spl21.bookstore.entity.CartItem;
import io.github.spl21.bookstore.entity.Purchase;
import io.github.spl21.bookstore.entity.PurchaseItem;
import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.repository.UserRepository;
import io.github.spl21.bookstore.service.BookService;
import io.github.spl21.bookstore.service.CartService;
import io.github.spl21.bookstore.service.PurchaseService;

@Controller
@RequestMapping("/checkout")
public class CheckoutController {

    private final BookService bookService;
    private final CartService cartService;
    private final PurchaseService purchaseService;
    private final UserRepository userRepository;

    public CheckoutController(
            PurchaseService purchaseService,
            CartService cartService,
            BookService bookService,
            UserRepository userRepository) {

        this.purchaseService = purchaseService;
        this.cartService = cartService;
        this.bookService = bookService;
        this.userRepository = userRepository;
    }

    private Optional<User> getCurrentUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        return userRepository.findByUsername(authentication.getName());
    }

    @GetMapping
    public String showCheckOutForm(
            Authentication authentication,
            Model model) {

        Optional<User> userOptional = getCurrentUser(authentication);

        if (userOptional.isEmpty()) {
            return "redirect:/auth/login";
        }

        User user = userOptional.get();

        Cart cart = cartService.getCartByUser(user);

        if (cart == null) {
            return "redirect:/cart";
        }

        List<CartItem> cartItems = cartService.getCartItems(user);

        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        List<String> outOfStockMessages = new ArrayList<>();
        double totalAmount = 0;

        for (CartItem item : cartItems) {
            Book book = item.getBook();

            totalAmount += item.getQuantity() * book.getPrice();

            if (item.getQuantity() > book.getCopies()) {
                outOfStockMessages.add(
                        book.getTitle() + " is out of stock (Available: "
                                + book.getCopies() + ")");
            }
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);

        if (!outOfStockMessages.isEmpty()) {
            model.addAttribute("outOfStockWarnings", outOfStockMessages);
            return "customer/cart";
        }

        return "customer/checkout";
    }

    @PostMapping("/placeOrder")
    public String placeOrder(
            @RequestParam("address") String address,
            @RequestParam("phone") String phone,
            Authentication authentication,
            Model model) {

        Optional<User> userOptional = getCurrentUser(authentication);

        if (userOptional.isEmpty()) {
            return "redirect:/auth/login";
        }

        if (address == null || address.isBlank()
                || phone == null
                || !phone.matches("[0-9]{10}")) {

            model.addAttribute("checkoutError",
                    "Please provide a valid address and 10-digit phone number.");

            return "customer/checkout";
        }

        User currentUser = userOptional.get();

        Cart cart = cartService.getCartByUser(currentUser);

        List<CartItem> cartItems = cart != null
                ? cartService.getCartItems(currentUser)
                : new ArrayList<>();

        if (cartItems.isEmpty()) {
            return "redirect:/cart";
        }

        // Step 1: Check stock
        List<String> stockErrors = new ArrayList<>();

        for (CartItem item : cartItems) {
            Book book = item.getBook();

            if (item.getQuantity() > book.getCopies()) {
                stockErrors.add(
                        book.getTitle() + " (Available: "
                                + book.getCopies() + ")");
            }
        }

        if (!stockErrors.isEmpty()) {
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("totalAmount",
                    cartItems.stream()
                            .mapToDouble(i -> i.getQuantity()
                                    * i.getBook().getPrice())
                            .sum());
            model.addAttribute("stockErrors", stockErrors);

            return "customer/checkout";
        }

        // Step 2: Create Purchase
        Purchase purchase = new Purchase();
        purchase.setUser(currentUser);
        purchase.setPurchaseDate(LocalDateTime.now());
        purchase.setAddress(address);
        purchase.setPhone(phone);

        purchase.setTotalAmount(
                cartItems.stream()
                        .mapToDouble(i -> i.getQuantity()
                                * i.getBook().getPrice())
                        .sum());

        List<PurchaseItem> purchaseItems = new ArrayList<>();

        // Step 3: Create PurchaseItems and update stock
        for (CartItem item : cartItems) {
            Book book = item.getBook();

            book.setCopies(book.getCopies() - item.getQuantity());
            bookService.updateBook(book);

            PurchaseItem purchaseItem = new PurchaseItem();
            purchaseItem.setBook(book);
            purchaseItem.setQuantity(item.getQuantity());
            purchaseItem.setPurchase(purchase);
            purchaseItem.setPriceAtPurchase(book.getPrice());

            purchaseItems.add(purchaseItem);
        }

        purchase.setItems(purchaseItems);

        purchaseService.createPurchase(purchase);

        // Step 4: Clear cart
        cartService.clearCart(currentUser);

        // Step 5: Redirect to success page
        return "redirect:/checkout/order_success";
    }

    @GetMapping("/order_success")
    public String showOrderSuccessPage() {
        return "customer/order_success";
    }
}