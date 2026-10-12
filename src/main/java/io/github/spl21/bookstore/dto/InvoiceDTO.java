package io.github.spl21.bookstore.dto;

import java.time.LocalDateTime;
import java.util.List;

import io.github.spl21.bookstore.entity.InventoryReservation;
import lombok.Getter;
import lombok.Setter;


@Setter 
@Getter 
public class InvoiceDTO {
    
    String fullName;
    String address;
    String email;
    String phone;
    double totalAmount;
    List<InventoryReservation> invoiceItems;
    LocalDateTime generatedAt;
    String stripeUrl;

    // no args constructor
    public InvoiceDTO() {}

}
