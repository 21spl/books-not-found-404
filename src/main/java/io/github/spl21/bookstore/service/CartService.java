package io.github.spl21.bookstore.service;

import java.util.*;

import io.github.spl21.bookstore.entity.*;
import io.github.spl21.bookstore.repository.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class CartService {
    
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private BookRepository bookRepository;
    
    @Transactional(readOnly = true)
    public Cart getCartByUser(User user) {
        Long userId = user.getUserId(); // Migrated to Long
        return cartRepository.findByUser_UserId(userId).orElse(null);
    }
    
    public void addBookToCart(User user, Book book, int quantity) {
        Cart cart = cartRepository.findByUser_UserId(user.getUserId()).orElse(null);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart = cartRepository.save(cart); // Assigned back to capture generated ID
        }
        
        Long bookId = book.getBookId(); // Migrated to Long
        Long cartId = cart.getCartId(); // Migrated to Long
        CartItem existingItem = cartItemRepository.findByBook_BookIdAndCart_CartId(bookId, cartId).orElse(null);
        
        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            if (newQuantity > book.getCopies()) {
                newQuantity = book.getCopies();
            }
            
            existingItem.setQuantity(newQuantity);
            existingItem.setSubTotal(newQuantity * book.getPrice());
            cartItemRepository.save(existingItem); // save() acts as update
        } else {
            int actualQuantity = Math.min(quantity, book.getCopies());
            CartItem newItem = new CartItem();
            newItem.setBook(book);
            newItem.setCart(cart);
            newItem.setQuantity(actualQuantity); // Fixed bug: sets actual quantity restricted by stock
            newItem.setSubTotal(actualQuantity * book.getPrice());
            cartItemRepository.save(newItem);
        }
        
        // Recalculate and update cart total using standard clean batch fetches
        List<CartItem> cartItems = cartItemRepository.findByCart_CartId(cart.getCartId());
        double total = cartItems.stream()
                .mapToDouble(CartItem::getSubTotal)
                .sum();
        cart.setTotalAmount(total);
        cartRepository.save(cart);
    }
    
    public void updateCartItemQuantity(Long cartItemId, int quantity) { // Migrated to Long
        CartItem item = cartItemRepository.findById(cartItemId).orElse(null);
        if (item != null) {
            item.setQuantity(quantity);
            cartItemRepository.save(item);
        }  
    }
    
    public void removeCartItem(CartItem item) {
        cartItemRepository.delete(item);
    }
    
    public void clearCart(User user) {
        Cart cart = cartRepository.findByUser_UserId(user.getUserId()).orElse(null);
        if (cart != null) {
            List<CartItem> items = cartItemRepository.findByCart_CartId(cart.getCartId());
            // Performance Optimization: Replaced iterative loops with single DB batch statement
            cartItemRepository.deleteAllInBatch(items);
        }
    }
    
    public void updateCartItem(CartItem item) {
        cartItemRepository.save(item);
    }
    
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(User user) {
        Cart cart = cartRepository.findByUser_UserId(user.getUserId()).orElse(null);
        if (cart != null) {
            return cartItemRepository.findByCart_CartId(cart.getCartId());
        }
        return new ArrayList<>();
    }
    
    @Transactional(readOnly = true)
    public double calculateCartTotal(User user) {
        double total = 0.0;
        List<CartItem> items = getCartItems(user);
        for (CartItem item : items) {
            total += item.getBook().getPrice() * item.getQuantity();
        }
        return total;
    }
}
