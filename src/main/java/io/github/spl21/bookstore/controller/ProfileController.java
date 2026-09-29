package io.github.spl21.bookstore.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.github.spl21.bookstore.entity.Purchase;
import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.service.PurchaseService;
import jakarta.servlet.http.HttpSession;

@Controller
public class ProfileController {
    
    @Autowired
    private PurchaseService purchaseService;

    // on clicking profile from navbar
    @GetMapping("/profile")
    public String showProfile(Model model, HttpSession session) {
        User loggedInUser = (User) session.getAttribute("loggedInUser");
        
        // Added intercept fallback to protect your entity getters from crashing if the session expires
        if (loggedInUser == null) {
            return "redirect:/login";
        }
        
        List<Purchase> userPurchases = purchaseService.findAllPurchaseByUserId(loggedInUser.getUserId());

        model.addAttribute("user", loggedInUser);
        model.addAttribute("purchases", userPurchases);

        return "profile";
    }
    
    // show all purchases
    @GetMapping("/profile/purchase_history")
    public String viewAllPurchases(HttpSession session, Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        
        List<Purchase> purchases = purchaseService.findAllPurchaseByUserId(user.getUserId());
        model.addAttribute("purchases", purchases);
        return "purchase_history"; 
    }
    
    // show purchase details of a single purchase
    @GetMapping("/profile/purchase_details/{id}")
    public String viewPurchaseDetails(@PathVariable("id") Long purchaseId, // Updated identifier type to Long
                                      HttpSession session,
                                      Model model) {
        User user = (User) session.getAttribute("loggedInUser");
        if (user == null) {
            return "redirect:/login";
        }
        
        Purchase purchase = purchaseService.findByIdWithItems(purchaseId);
        if (purchase == null) {
            return "redirect:/error500";
        }
        
        model.addAttribute("loggedInUser", user); 
        model.addAttribute("purchase", purchase);
        return "purchase_details";
    }
}

