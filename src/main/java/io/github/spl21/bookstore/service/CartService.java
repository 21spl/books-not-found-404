package io.github.spl21.bookstore.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.spl21.bookstore.entity.Book;
import io.github.spl21.bookstore.entity.Cart;
import io.github.spl21.bookstore.entity.CartItem;
import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.exception.NotEnoughStockException;
import io.github.spl21.bookstore.exception.OutOfStockException;
import io.github.spl21.bookstore.exception.ResourceNotFoundException;
import io.github.spl21.bookstore.repository.CartItemRepository;
import io.github.spl21.bookstore.repository.CartRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor 
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final UserService userService;
    private final BookService bookService;

    @Transactional(readOnly = true)
    public Cart getCartByUser(User user) {
        Long userId = user.getId(); 
        return cartRepository.findByUser_Id(userId).orElse(null);
    }

    @Transactional(readOnly = true)
    public Cart getCartByUsername(String username) {
        Long userId = userService.findUserByUsername(username).getId();
        return cartRepository.findByUser_Id(userId).orElse(null);
    }


    //======================================Add Book to cart======================================================
    @Transactional
    public void addBookToCart(User user, Book book, int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("Quantity must be at least 1.");
        }

        // step 1: Make the cart ready ----------------------------------------------------
        Cart cart = cartRepository.findByUser_Id(user.getId()).orElse(null);
        if (cart == null) {
            cart = new Cart();
            cart.setUser(user);
            cart = cartRepository.save(cart); 
            user.setCart(cart);
        }

        // step 2: get actual stock --------------------------------------------------------
        int actualStock = bookService.getActualStock(book.getId());
        if (actualStock < 1) {
            throw new OutOfStockException();
        }

        Long bookId = book.getId(); 
        Long cartId = cart.getId(); 

        // step 3: check if the item already exists
        CartItem existingItem = cartItemRepository.findByBook_IdAndCart_Id(bookId, cartId).orElse(null);
        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            
            // FIX 3: Safety verification check against cumulative cart modifications
            if (actualStock < newQuantity) {
                throw new NotEnoughStockException(newQuantity, actualStock);
            }
            
            existingItem.setQuantity(newQuantity);
            existingItem.setSubTotal(newQuantity * book.getPrice());
            cartItemRepository.save(existingItem);
        } else {
            if (actualStock < quantity) {
                throw new NotEnoughStockException(quantity, actualStock);
            }
            CartItem newItem = new CartItem();
            newItem.setBook(book);
            newItem.setCart(cart);
            newItem.setQuantity(quantity); 
            newItem.setSubTotal(quantity * book.getPrice());
            cartItemRepository.save(newItem);
        }
        
        // step 4: update the cart calculations
        updateCart(cart);
    }

    //============================== Update Cart Item===============================================
    @Transactional
    public void updateCartItem(CartItem item) {
        Book book = item.getBook();

        // step 1: get actual stock --------------------------------------------------------
        int actualStock = bookService.getActualStock(book.getId());
        if (actualStock < 1) {
            throw new OutOfStockException();
        }
        
        // FIX 2: Extracted quantity from item parameter structure explicitly
        if (actualStock < item.getQuantity()) {
            throw new NotEnoughStockException(item.getQuantity(), actualStock);
        }

        // step 2: update the item calculations --------------------------------------------
        item.setSubTotal(item.getQuantity() * book.getPrice());
        Cart cart = item.getCart();
        cartItemRepository.save(item);

        // step 3: update the cart calculations --------------------------------------------
        updateCart(cart);
    }

    //================================ Update Cart Item Quantity=============================================
    @Transactional
    public void updateCartItemQuantity(Long itemId, int quantity) {
        CartItem item = cartItemRepository.findById(itemId)
                .orElseThrow(() -> new ResourceNotFoundException("Cart item reference data not found"));
        Book book = item.getBook();

        // step 1: get actual stock --------------------------------------------------------
        int actualStock = bookService.getActualStock(book.getId());
        if (actualStock < 1) {
            throw new OutOfStockException();
        }
        if (actualStock < quantity) {
            throw new NotEnoughStockException(quantity, actualStock);
        }

        // step 2: update the item parameters -----------------------------------------------
        item.setQuantity(quantity);
        item.setSubTotal(quantity * book.getPrice());
        Cart cart = item.getCart();
        cartItemRepository.save(item);

        // step 3: update the cart calculations ---------------------------------------------
        updateCart(cart);
    }

    //================================ Get Cart Items=============================================
    @Transactional(readOnly = true)
    public List<CartItem> getCartItems(User user) {
        Cart cart = cartRepository.findByUser_Id(user.getId()).orElse(null);
        if (cart == null) {
            return new ArrayList<>();
        }
        
        List<CartItem> cartItems = cartItemRepository.findByCart_Id(cart.getId());

        for (CartItem item : cartItems) {
            Book book = item.getBook();
            int actualStock = bookService.getActualStock(book.getId());

            // Dynamic Transient @Transient view attachment inject
            if (actualStock < 5) {
                item.setStockWarning("Hurry! Only " + actualStock + " available copies left in stock.");
            }
        }

        return cartItems;
    }

    //================================ Calculate Cart Total=============================================
    @Transactional(readOnly = true)
    public double calculateCartTotal(User user) {
        double total = 0.0;
        List<CartItem> items = getCartItems(user);
        for (CartItem item : items) {
            total += item.getBook().getPrice() * item.getQuantity();
        }
        return total;
    }

    //===================================== Remove Cart Item=============================================
    @Transactional
    public void removeCartItem(CartItem item) {
        Cart cart = item.getCart();
        cartItemRepository.delete(item);
        cartItemRepository.flush();

        updateCart(cart);
    }

    //===================================== Clear Cart=============================================
    @Transactional
    public void clearCart(User user) {
        Cart cart = cartRepository.findByUser_Id(user.getId()).orElse(null);
        if (cart != null) {
            List<CartItem> items = cartItemRepository.findByCart_Id(cart.getId());
            cartItemRepository.deleteAllInBatch(items);

            cart.setTotalAmount(0.0);
            cartRepository.save(cart);
        }
    }

    //===================================== Private Cart Updater Helper Method===================
    //  Completed cutoff business math logic via a clean private helper method
    private void updateCart(Cart cart) {
        List<CartItem> remainingItems = cartItemRepository.findByCart_Id(cart.getId());
        double total = remainingItems.stream()
                .mapToDouble(CartItem::getSubTotal)
                .sum();

        cart.setTotalAmount(total);
        cartRepository.save(cart);
    }
}
