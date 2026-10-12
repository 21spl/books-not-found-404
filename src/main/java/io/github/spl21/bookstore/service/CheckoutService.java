package io.github.spl21.bookstore.service;

import org.springframework.stereotype.Service;

import io.github.spl21.bookstore.repository.UserRepository;
import io.github.spl21.bookstore.service.InventoryReservationService;
import lombok.RequiredArgsConstructor;

@Service 
@RequiredArgsConstructor 
public class CheckoutService {

    private final UserRepository userRepository;
    private final CartService cartService;
    private final PurchaseService purchaseService;
    private final InventoryReservationService inventoryReservationService;

}
