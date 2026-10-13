package io.github.spl21.bookstore.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.EventDataObjectDeserializer;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;

import io.github.spl21.bookstore.service.FulfillmentService;

@Controller
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
    @ResponseBody 
    public ResponseEntity<String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        Event event;

        try {
            // Security Guard: Verify the request origin and cryptographic signature integrity
            event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
        } catch (SignatureVerificationException e) {
            System.err.println("CRITICAL: Webhook signature validation failed. Check your signing secret.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature token validation");
        }

        System.out.println("Webhook verified successfully! Event type captured: " + event.getType());

        // Handle the specific payment confirmation event type
        if ("checkout.session.completed".equals(event.getType())) {
            EventDataObjectDeserializer deserializer = event.getDataObjectDeserializer();

            // This executes perfectly on version 28+ because the model classes match the payload definitions
            if (deserializer.getObject().isPresent()) {
                Session session = (Session) deserializer.getObject().get();
                System.out.println("Stripe payload parsed correctly! Initiating fulfillment for session: " + session.getId());
                
                // Trigger the core database transaction using the session identifier token
                fulfillmentService.fulfillOrder(session.getId());
            } else {
                System.err.println("ERROR: Failed to deserialize checkout session payload data mapping.");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Failed to parse event object model");
            }
        }

        // Acknowledge receipt of the webhook back to Stripe with a clean 200 OK
        return ResponseEntity.ok("Success");
    }
}
