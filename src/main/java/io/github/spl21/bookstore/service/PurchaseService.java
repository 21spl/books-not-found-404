package io.github.spl21.bookstore.service;

import io.github.spl21.bookstore.entity.*;
import io.github.spl21.bookstore.repository.*;




import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PurchaseService {
    
    @Autowired
    private PurchaseRepository purchaseRepository;
    
    public void createPurchase(Purchase purchase) {
        purchaseRepository.save(purchase);
    }
    
    @Transactional(readOnly = true)
    public Purchase findById(Long purchaseId) { // Updated to Long
        return purchaseRepository.findById(purchaseId).orElse(null);
    }
    
    @Transactional(readOnly = true)
    public List<Purchase> findAllPurchaseByUserId(Long userId) { // Updated to Long
        return purchaseRepository.findByUser_UserIdOrderByPurchaseDateDesc(userId);
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
}
