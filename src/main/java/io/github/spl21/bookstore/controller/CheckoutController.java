


package io.github.spl21.bookstore.controller;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import io.github.spl21.bookstore.dto.CheckoutSummaryDTO;
import io.github.spl21.bookstore.dto.DeliveryFormDto;
import io.github.spl21.bookstore.exception.EmptyCartException;
import io.github.spl21.bookstore.exception.NotEnoughStockException;
import io.github.spl21.bookstore.exception.UserNotFoundException;
import io.github.spl21.bookstore.service.CheckoutService;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/checkout")
public class CheckoutController {

    private final CheckoutService checkoutService;

//===================================== Process Checkout ==============================================
    @GetMapping()
    public String processCheckout(Authentication authentication, Model model) {

        // step 1: call checkoutService.reserveItemsForCheckout to reserve the inventory
        try{
            checkoutService.reserveItemsForCheckout(authentication.getName());
        } catch (UserNotFoundException e) {
            return "redirect:/auth/login";    
        }catch (EmptyCartException | NotEnoughStockException e) {
            return "redirect:/cart";
        }
        
        // step 2: call checkoutService.getCheckoutSummary to get the checkout summary
        try {
            CheckoutSummaryDTO summary = checkoutService.getCheckoutSummary(authentication.getName());
            model.addAttribute("cartItems", summary.getCartItems());
            model.addAttribute("totalAmount", summary.getTotalAmount());
            // add the DeliveryFormDto to the model
            model.addAttribute("deliveryForm", new DeliveryFormDto());

            if (!summary.getStockWarnings().isEmpty()) {
                model.addAttribute("outOfStockWarnings", summary.getStockWarnings());
            }
            return "customer/checkout";
        } catch (UserNotFoundException e) {
            return "redirect:/auth/login";
        } catch (EmptyCartException | NotEnoughStockException e) {
            return "redirect:/cart";
        }
    }

//====================================== Show Delivery Details Form ================================

    @GetMapping("/delivery-details")
    public String showDeliveryDetailsForm(Authentication authentication, Model model) {

        model.addAttribute("deliveryForm", new DeliveryFormDto());
        return "customer/delivery_details";
    }

    @PostMapping("/placeOrder")
    public String placeOrder(
            @Valid @ModelAttribute("deliveryForm") DeliveryFormDto form, 
            BindingResult bindingResult,                       
            Authentication authentication, 
            Model model) {

        if (bindingResult.hasErrors()) {
            return "customer/delivery_details";
        }

        try {
            // Get the raw redirect URL string directly from the service layer
            String stripeUrl = checkoutService.initiateCheckoutPayment(authentication.getName(), form);
            return "redirect:" + stripeUrl;
        } catch (EmptyCartException e) {
            return "redirect:/cart";
        }
    }

    @GetMapping("/success")
    public String showOrderSuccessPage() {
        return "customer/order_success";
    }

    @GetMapping("/cancel")
    public String showOrderCancelPage() {
        return "customer/order_cancel";
    }    
}


