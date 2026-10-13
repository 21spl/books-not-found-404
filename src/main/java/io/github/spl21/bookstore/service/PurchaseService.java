package io.github.spl21.bookstore.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.spl21.bookstore.entity.Purchase;
import io.github.spl21.bookstore.enums.PaymentStatus;
import io.github.spl21.bookstore.repository.PurchaseRepository;

@Service
@Transactional
public class PurchaseService {
    
    private final PurchaseRepository purchaseRepository;

    PurchaseService(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }
    
    public void createPurchase(Purchase purchase) {
        purchaseRepository.save(purchase);
    }
    
    @Transactional(readOnly = true)
    public Purchase findById(Long purchaseId) { // Updated to Long
        return purchaseRepository.findById(purchaseId).orElse(null);
    }
    
    @Transactional(readOnly = true)
    public List<Purchase> findAllPurchaseByUserId(Long userId) { // Updated to Long
        return purchaseRepository.findByUser_IdOrderByPurchaseDateDesc(userId);
    }
    
    @Transactional(readOnly = true)
    public List<Purchase> findAllPurchases() {
        return purchaseRepository.findAll();
    }
    
    @Transactional(readOnly = true)
    public Purchase findByIdWithItems(Long purchaseId) { // Updated to Long
        return purchaseRepository.findByIdWithItems(purchaseId).orElse(null);
    }
    
    @Transactional(readOnly = true)
    public List<Purchase> getAllPurchasesWithDetails() {
        return purchaseRepository.findAllPurchasesWithUserAndItems();
    }


    // find all purchases for a user where payment status is paid
    public List<Purchase> findSuccessfulPurchases(Long userId) {
        return purchaseRepository.findByUser_IdAndPaymentStatus(userId, PaymentStatus.PAID);
    }
}
