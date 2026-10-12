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
import lombok.RequiredArgsConstructor;


@Service
@Transactional
@RequiredArgsConstructor 
public class CartService {
    
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public Cart getCartByUser(User user) {
        Long userId = user.getId(); // Migrated to Long
        return cartRepository.findByUser_Id(userId).orElse(null);
    }

    @Transactional(readOnly = true)
    public Cart getCartByUsername(String username) {
        Long userId = userService.findUserByUsername(username).getId();
        return cartRepository.findByUser_Id(userId).orElse(null);
    }


    @Transactional 
    public void addBookToCart(User user, Book book, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }

        if (book.getCopies() < 1) {
            throw new IllegalArgumentException("This book is out of stock.");
        }
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
                throw new IllegalArgumentException("Requested quantity exceeds available stock.");
            }
            
            existingItem.setQuantity(newQuantity);
            existingItem.setSubTotal(newQuantity * book.getPrice());
            cartItemRepository.save(existingItem); // save() acts as update
        } else {
            int actualQuantity = quantity;
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
    
    public void updateCartItemQuantity(Long cartItemId, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }

        CartItem item = cartItemRepository.findById(cartItemId).orElse(null);

        if (item == null) {
            return;
        }

        if (quantity > item.getBook().getCopies()) {
            throw new IllegalArgumentException(
                    "Requested quantity exceeds available stock.");
        }

        item.setQuantity(quantity);
        item.setSubTotal(quantity * item.getBook().getPrice());

        Cart cart = item.getCart();

        List<CartItem> cartItems = cartItemRepository.findByCart_Id(cart.getId());

        double total = cartItems.stream()
                .mapToDouble(cartItem ->
                        cartItem.getId().equals(item.getId())
                                ? item.getSubTotal()
                                : cartItem.getSubTotal())
                .sum();

        cart.setTotalAmount(total);
        cartRepository.save(cart);
    }
    
    public void removeCartItem(CartItem item) {
        Cart cart = item.getCart();

        cartItemRepository.delete(item);
        cartItemRepository.flush();

        List<CartItem> remainingItems =
                cartItemRepository.findByCart_Id(cart.getId());

        double total = remainingItems.stream()
                .mapToDouble(CartItem::getSubTotal)
                .sum();

        cart.setTotalAmount(total);
        cartRepository.save(cart);
    
    }
    
    public void clearCart(User user) {
        Cart cart = cartRepository.findByUser_Id(user.getId()).orElse(null);

        if (cart != null) {
            List<CartItem> items = cartItemRepository.findByCart_Id(cart.getId());
            cartItemRepository.deleteAllInBatch(items);

            cart.setTotalAmount(0.0);
            cartRepository.save(cart);
        }
    }
    
    public void updateCartItem(CartItem item) {
        item.setSubTotal(item.getQuantity() * item.getBook().getPrice());
        Cart cart = item.getCart();
        cartItemRepository.save(item);

        List<CartItem> cartItems = cartItemRepository.findByCart_Id(cart.getId());

        double total = cartItems.stream()
                .mapToDouble(cartItem ->
                        cartItem.getId().equals(item.getId())
                                ? item.getSubTotal()
                                : cartItem.getSubTotal())
                .sum();

        cart.setTotalAmount(total);
        cartRepository.save(cart);
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
