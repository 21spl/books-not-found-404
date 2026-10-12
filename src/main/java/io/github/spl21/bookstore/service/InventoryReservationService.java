package io.github.spl21.bookstore.service;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.spl21.bookstore.entity.Book;
import io.github.spl21.bookstore.entity.InventoryReservation;
import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.exception.ResourceNotFoundException;
import io.github.spl21.bookstore.exception.UserNotFoundException;
import io.github.spl21.bookstore.repository.BookRepository;
import io.github.spl21.bookstore.repository.InventoryReservationRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional 
public class InventoryReservationService{

    private final UserService userService;
    private final BookRepository bookRepository;
    private final InventoryReservationRepository reservationRepository;

    @Transactional 
    public boolean reserveInventory(Long bookId, Long userId, int quantity){

        // 1. Lock the Book row directly from the repo. 
        // This transaction explicitly holds the PESSIMISTIC_WRITE lock.
        Book book = bookRepository.findByIdWithPessimisticWrite(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        // 2. Delegate user lookup to its legitimate domain service
        User user = userService.findUserById(userId);
        if(user == null){
            throw new UserNotFoundException("User not found with ID: " + userId);
        }

        // 3. Evaluate active reservations 
        int reserved = reservationRepository.getActiveReservedQuantity(bookId, LocalDateTime.now());
        int realAvailableStock = book.getCopies() - reserved;

        // 4. Secure reservation if stock matches
        if (realAvailableStock >= quantity) {
            InventoryReservation reservation = new InventoryReservation(book, user, quantity, 10);
            reservationRepository.save(reservation);
            return true; 
        }

        return false; 
    }
    

}