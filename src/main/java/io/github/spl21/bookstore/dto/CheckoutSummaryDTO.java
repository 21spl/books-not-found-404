package io.github.spl21.bookstore.dto;



import io.github.spl21.bookstore.entity.CartItem;
import lombok.Getter;

import java.util.List;

@Getter
public class CheckoutSummaryDTO {
    private final List<CartItem> cartItems;
    private final double totalAmount;
    private final List<String> stockWarnings;

    public CheckoutSummaryDTO(List<CartItem> cartItems, double totalAmount, List<String> stockWarnings) {
        this.cartItems = cartItems;
        this.totalAmount = totalAmount;
        this.stockWarnings = stockWarnings;
    }

}
