package io.github.spl21.bookstore.controller;


import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import io.github.spl21.bookstore.service.FulfillmentService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/webhooks")
public class StripeWebhookController {

    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    private final FulfillmentService fulfillmentService;

    public StripeWebhookController(FulfillmentService fulfillmentService) {
        this.fulfillmentService = fulfillmentService;
    }

    /**
     * Public unauthenticated webhook listener endpoint targeted by Stripe servers.
     */
    @PostMapping("/stripe")
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            // Security Guard: Verify the request origin and cryptographic signature integrity
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            // Reject fake or tampered payloads immediately
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature token validation");
        }

        // Handle the specific payment confirmation event type
        if ("checkout.session.completed".equals(event.getType())) {
            // Extract the session data payload structure
            Session session = (Session) event.getDataObjectDeserializer().getObject()
                    .orElse(null);

            if (session != null) {
                // Trigger the core database transaction using the session identifier token
                fulfillmentService.fulfillOrder(session.getId());
            }
        }

        // Acknowledge receipt of the webhook back to Stripe with a clean 200 OK
        return ResponseEntity.ok("Success");
    }
}
