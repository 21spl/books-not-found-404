package io.github.spl21.bookstore.service;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import io.github.spl21.bookstore.entity.Purchase;
import io.github.spl21.bookstore.entity.PurchaseItem;
import io.github.spl21.bookstore.exception.PaymentGatewayException;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.URISyntaxException;

@Service
public class StripeService {

    @Value("${stripe.api.key}")
    private String apiKey;
 
    @Value("${stripe.success.url}")
    private String successUrl;

    @Value("${stripe.cancel.url}")
    private String cancelUrl;

    @PostConstruct
    public void init() {
        // Initialize the Stripe context globally upon application initialization
        Stripe.apiKey = this.apiKey;
    }

    /**
     * Generates a hosted Stripe checkout billing gate link.
     */
    public String createCheckoutSession(Purchase purchase) {
        SessionCreateParams.Builder sessionBuilder = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl)
                .setCustomerEmail(purchase.getUser().getEmail()); // Auto-fill user email on Stripe gate

        // Add each immutable snapshot item into the Stripe billing builder mapping
        for (PurchaseItem item : purchase.getItems()) {
            // Conversion Rule: Stripe processes financial amounts in sub-units (e.g., Paise for INR)
            long unitAmountInSubUnits = Math.round(item.getPriceAtPurchase() * 100);

            sessionBuilder.addLineItem(
                SessionCreateParams.LineItem.builder()
                    .setQuantity((long) item.getQuantity())
                    .setPriceData(
                        SessionCreateParams.LineItem.PriceData.builder()
                            .setCurrency("inr") // Set to Indian Rupee (or your target currency)
                            .setUnitAmount(unitAmountInSubUnits)
                            .setProductData(
                                SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                    .setName(item.getBook().getTitle())
                                    .build()
                            )
                            .build()
                    )
                    .build()
            );
        }

        try {
            // Send API call out to Stripe servers to establish the transaction frame
            Session session = Session.create(sessionBuilder.build());
            return session.getUrl(); // Returns the unique hosted billing portal redirect link

        } catch (StripeException e) {
            throw new PaymentGatewayException("Failed to establish processing terminal link with Stripe", e);
        }
    }

    
    // Extracts the raw 'cs_test_...' identification token from a complete Stripe redirect URL.

    public String extractSessionIdFromUrl(String url) {
        try {
            URI uri = new URI(url);
            String path = uri.getPath();
            // Stripe payment links standard: /c/pay/cs_test_XYZ...
            String[] segments = path.split("/");
            for (String segment : segments) {
                if (segment.startsWith("cs_")) {
                    return segment;
                }
            }
            throw new PaymentGatewayException("Invalid transaction mapping token parsed from checkout session wrapper");
        } catch (URISyntaxException | NullPointerException e) {
            throw new PaymentGatewayException("Failed to decode remote tracking metadata tokens from gateway URL", e);
        }
    }
}
