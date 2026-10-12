package io.github.spl21.bookstore.service;

import java.time.LocalDateTime;
import java.util.List;

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
public class InventoryReservationService {

    private final UserService userService;
    private final BookRepository bookRepository;
    private final InventoryReservationRepository reservationRepository;

    @Transactional 
    public boolean reserveInventory(Long bookId, Long userId, int quantity) {
        Book book = bookRepository.findByIdWithPessimisticWrite(bookId)
            .orElseThrow(() -> new ResourceNotFoundException("Book not found with ID: " + bookId));

        User user = userService.findUserById(userId);
        if (user == null) {
            throw new UserNotFoundException("User not found with ID: " + userId);
        }

        int preReserved = reservationRepository.getActiveReservedQuantity(bookId, LocalDateTime.now());
        int realAvailableStock = book.getCopies() - preReserved;

        if (realAvailableStock >= quantity) {
            InventoryReservation reservationObject = new InventoryReservation(book, user, quantity);
            reservationRepository.save(reservationObject);
            return true;
        }
        return false; 
    }
    
    @Transactional(readOnly = true)
    public List<InventoryReservation> getAllReservationsByUsername(String username) {
        Long userId = userService.getUserIdByUsername(username);
        return reservationRepository.findByUserIdAndExpiresAtAfter(userId, LocalDateTime.now());
    }
}
