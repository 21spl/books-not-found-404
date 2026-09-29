package io.github.spl21.bookstore.repository;

import io.github.spl21.bookstore.entity.PurchaseItem;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface PurchaseItemRepository extends JpaRepository<PurchaseItem, Long> {


    List<PurchaseItem> findByPurchasePurchaseId(Long purchaseId);

  
}
