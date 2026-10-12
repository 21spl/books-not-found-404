package io.github.spl21.bookstore.config;

import java.time.LocalDateTime;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import io.github.spl21.bookstore.repository.InventoryReservationRepository;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor 
public class InventoryCleanupWorker {

    private final InventoryReservationRepository reservationRepository;

    // Runs automatically every 60 seconds
    @Scheduled(fixedRate = 60000)
    @Transactional
    public void cleanExpiredReservations() {
        reservationRepository.deleteExpiredReservations(LocalDateTime.now());
    }
} 
