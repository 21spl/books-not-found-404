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
import io.github.spl21.bookstore.dto.OrderFormDto;
import io.github.spl21.bookstore.exception.EmptyCartException;
import io.github.spl21.bookstore.exception.NotEnoughStockException;
import io.github.spl21.bookstore.exception.OutOfStockException;
import io.github.spl21.bookstore.exception.UserNotFoundException;
import io.github.spl21.bookstore.service.CheckoutService;
import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/checkout")
@RequiredArgsConstructor 
public class CheckoutController {

   private final CheckoutService checkoutService;

   @GetMapping()
   public String showCheckoutForm(Authentication authentication, Model model) {
        try{
            CheckoutSummaryDTO summary = checkoutService.getCheckoutSummary(authentication.getName());

            model.addAttribute("cartItems", summary.getCartItems());
            model.addAttribute("totalAmount", summary.getTotalAmount());

            if (!summary.getStockWarnings().isEmpty()) {
                model.addAttribute("outOfStockWarnings", summary.getStockWarnings());
                return "customer/cart";
            }
            return "customer/checkout";

        }catch(UserNotFoundException e){
            return "redirect:/auth/login";
        } catch (EmptyCartException e) {
            return "redirect:/cart";
        } catch(NotEnoughStockException e){
            return "redirect:/cart";
        }
        
   }


   @PostMapping("/placeOrder")
    public String placeOrder(
            @Valid @ModelAttribute("orderForm") OrderFormDto form, 
            BindingResult bindingResult,                       
            Authentication authentication, 
            Model model) {

        // If validations failed, skip business logic and return the error view immediately
        if (bindingResult.hasErrors()) {
            CheckoutSummaryDTO summary = checkoutService.getCheckoutSummary(authentication.getName());
            model.addAttribute("cartItems", summary.getCartItems());
            model.addAttribute("totalAmount", summary.getTotalAmount());
            return "customer/checkout";
        }

        try {
            checkoutService.placeOrder(authentication.getName(), form.getAddress(), form.getPhone());
            return "redirect:/checkout/order_success";
        } catch (OutOfStockException e) {
            return "customer/checkout";
        }
    }


    @GetMapping("/order_success")
    public String showOrderSuccessPage() {
        return "customer/order_success";
    }

    @GetMapping("/order_cancel)")
    public String showOrderCancelPage() {
        return "customer/order_cancel";
    }
}