package io.github.spl21.bookstore.controller;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.github.spl21.bookstore.entity.Book;
import io.github.spl21.bookstore.entity.Cart;
import io.github.spl21.bookstore.entity.CartItem;
import io.github.spl21.bookstore.entity.Purchase;
import io.github.spl21.bookstore.entity.PurchaseItem;
import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.service.BookService;
import io.github.spl21.bookstore.service.CartService;
import io.github.spl21.bookstore.service.PurchaseService;
import jakarta.servlet.http.HttpSession;

@Controller
public class CheckoutController {

    @Autowired 
    private BookService bookService;
    @Autowired 
    private CartService cartService;
    @Autowired 
    private PurchaseService purchaseService;

    @GetMapping("/checkout")
    public String showCheckOutForm(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }

        Cart cart = cartService.getCartByUser(user);
        if (cart == null) return "redirect:/cart";
        
        List<CartItem> cartItems = cartService.getCartItems(user);
        List<String> outOfStockMessages = new ArrayList<>();
        double totalAmount = 0;

        for (CartItem item : cartItems) {
            totalAmount += item.getQuantity() * item.getBook().getPrice();
            if (item.getQuantity() > item.getBook().getCopies()) {
                outOfStockMessages.add(item.getBook().getTitle() +
                        " is out of stock (Available: " + item.getBook().getCopies() + ")");
            }
        }

        if (!outOfStockMessages.isEmpty()) {
            model.addAttribute("outOfStockWarnings", outOfStockMessages);
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("totalAmount", totalAmount);
            return "cart";
        }

        model.addAttribute("cartItems", cartItems);
        model.addAttribute("totalAmount", totalAmount);
        return "checkout";
    }
    
    @PostMapping("/placeOrder")
    public String placeOrder(
            @RequestParam("address") String address,
            @RequestParam("phone") String phone,
            HttpSession session,
            Model model) {

        User currentUser = (User) session.getAttribute("loggedInUser");
        if (currentUser == null) {
            return "redirect:/login";
        }

        Cart cart = cartService.getCartByUser(currentUser);
        List<CartItem> cartItems = cart != null ? cartService.getCartItems(currentUser) : new ArrayList<>();

        // Step 1: Check stock
        List<String> stockErrors = new ArrayList<>();
        for (CartItem item : cartItems) {
            Book book = item.getBook();
            if (item.getQuantity() > book.getCopies()) {
                stockErrors.add(book.getTitle() + " (Available: " + book.getCopies() + ")");
            }
        }

        if (!stockErrors.isEmpty()) {
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("totalAmount", cartItems.stream().mapToDouble(i -> i.getQuantity() * i.getBook().getPrice()).sum());
            model.addAttribute("stockErrors", stockErrors);
            return "checkout";
        }

        // Step 2: Create Purchase
        Purchase purchase = new Purchase();
        purchase.setUser(currentUser);
        purchase.setPurchaseDate(LocalDateTime.now());
        

        purchase.setAddress(address);
        purchase.setPhone(phone);
        purchase.setTotalAmount(cartItems.stream().mapToDouble(i -> i.getQuantity() * i.getBook().getPrice()).sum());
        
        List<PurchaseItem> purchaseItems = new ArrayList<>();

        // Step 3: Create PurchaseItems + update stock
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
        purchaseService.createPurchase(purchase); // Handles the cascading persist perfectly

        // Step 4: Clear cart cleanly using our service batch optimization
        cartService.clearCart(currentUser);

        // Step 5: Redirect to success page
        return "redirect:/order_success";
    }
    
    @GetMapping("/order_success")
    public String showOrderSuccessPage() {
        return "order_success"; 
    }
}
