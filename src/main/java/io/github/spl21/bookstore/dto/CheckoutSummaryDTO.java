package io.github.spl21.bookstore.dto;



import java.util.List;

import io.github.spl21.bookstore.entity.CartItem;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CheckoutSummaryDTO {
    private final List<CartItem> cartItems;
    private final double totalAmount;
    private final List<String> stockWarnings;

   
}
