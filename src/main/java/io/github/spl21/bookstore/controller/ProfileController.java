package io.github.spl21.bookstore.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import io.github.spl21.bookstore.entity.Purchase;
import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.repository.UserRepository;
import io.github.spl21.bookstore.service.PurchaseService;


@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final PurchaseService purchaseService;
    private final UserRepository userRepository;
    

    public ProfileController(
            PurchaseService purchaseService,
            UserRepository userRepository) {
        this.purchaseService = purchaseService;
        this.userRepository = userRepository;
    }

    // Show the user's profile and recent purchases
    @GetMapping
    public String showProfile(Authentication authentication, Model model) {
        Optional<User> optionalUser = getCurrentUser(authentication);

        if (optionalUser.isEmpty()) {
            return "redirect:/auth/login";
        }

        User user = optionalUser.get();

        List<Purchase> userPurchases =
                purchaseService.findAllPurchaseByUserId(user.getId());

        model.addAttribute("user", user);
        model.addAttribute("purchases", userPurchases);

        return "customer/profile";
    }

    // Show all purchases belonging to the logged-in user
    @GetMapping("/purchase_history")
    public String viewAllPurchases(
            Authentication authentication,
            Model model) {

        Optional<User> optionalUser = getCurrentUser(authentication);

        if (optionalUser.isEmpty()) {
            return "redirect:/auth/login";
        }

        User user = optionalUser.get();

        List<Purchase> purchases =
                purchaseService.findSuccessfulPurchases(user.getId());

        model.addAttribute("purchases", purchases);

        return "customer/purchase_history";
    }

    // Show details of a purchase, only if it belongs to the logged-in user
    @GetMapping("/purchase_details/{id}")
    public String viewPurchaseDetails(
            @PathVariable("id") Long purchaseId,
            Authentication authentication,
            Model model) {

        Optional<User> optionalUser = getCurrentUser(authentication);

        if (optionalUser.isEmpty()) {
            return "redirect:/auth/login";
        }

        User user = optionalUser.get();

        Purchase purchase = purchaseService.findByIdWithItems(purchaseId);

        if (purchase == null) {
            return "redirect:/profile";
        }

        // Prevent users from viewing another user's purchase
        if (purchase.getUser() == null
                || !purchase.getUser().getId().equals(user.getId())) {
            return "redirect:/profile";
        }

        model.addAttribute("loggedInUser", user);
        model.addAttribute("purchase", purchase);

        return "customer/purchase_details";
    }

    // Resolve the authenticated username to the database User entity
    private Optional<User> getCurrentUser(Authentication authentication) {
        if (authentication == null
                || !authentication.isAuthenticated()
                || "anonymousUser".equals(authentication.getName())) {
            return Optional.empty();
        }

        return userRepository.findByUsername(authentication.getName());
    }
}