package io.github.spl21.bookstore.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.spl21.bookstore.entity.Book;
import io.github.spl21.bookstore.entity.Cart;
import io.github.spl21.bookstore.entity.CartItem;
import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.repository.CartItemRepository;
import io.github.spl21.bookstore.repository.CartRepository;


@Service
@Transactional
public class CartService {
    
    
    private final CartRepository cartRepository;
    
    private final CartItemRepository cartItemRepository;


    CartService(CartRepository cartRepository, CartItemRepository cartItemRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
    }
    
    @Transactional(readOnly = true)
    public Cart getCartByUser(User user) {
        Long userId = user.getId(); // Migrated to Long
        return cartRepository.findByUser_Id(userId).orElse(null);
    }
    
    public void addBookToCart(User user, Book book, int quantity) {
        Cart cart = cartRepository.findByUser_Id(user.getId()).orElse(null);
        if (cart == null) {
            cart = new Cart();

            cart.setUser(user);
            cart = cartRepository.save(cart); // Assigned back to capture generated ID

            user.setCart(cart);
        }
        
        Long bookId = book.getId(); // Migrated to Long
        Long cartId = cart.getId(); // Migrated to Long
        CartItem existingItem = cartItemRepository.findByBook_IdAndCart_Id(bookId, cartId).orElse(null);
        
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
        List<CartItem> cartItems = cartItemRepository.findByCart_Id(cart.getId());
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
        Cart cart = cartRepository.findByUser_Id(user.getId()).orElse(null);
        if (cart != null) {
            List<CartItem> items = cartItemRepository.findByCart_Id(cart.getId());
            // Performance Optimization: Replaced iterative loops with single DB batch statement
            cartItemRepository.deleteAllInBatch(items);
        }
    }
    
    public void updateCartItem(CartItem item) {
        cartItemRepository.save(item);
    }
    
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(User user) {
        Cart cart = cartRepository.findByUser_Id(user.getId()).orElse(null);
        if (cart != null) {
            return cartItemRepository.findByCart_Id(cart.getId());
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
