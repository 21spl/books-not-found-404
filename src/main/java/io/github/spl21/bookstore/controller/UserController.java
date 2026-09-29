package io.github.spl21.bookstore.controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import io.github.spl21.bookstore.dto.LoginResult;
import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.enums.Role;
import io.github.spl21.bookstore.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
    
    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("user") User user, BindingResult result, Model model) {
        if (result.hasErrors()) {
            return "register";
        }
        
        // Using type-safe Role Enum instead of string literals
        user.setRole(Role.CUSTOMER);
        
        boolean success = userService.registerUser(user);
        if (!success) {
            model.addAttribute("error", "Email already registered. Please Login.");
            return "redirect:/login";
        }
        return "redirect:/login";
    }
    
    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }
    
    @PostMapping("/login")
    public String processLogin(@RequestParam("email") String email, 
                               @RequestParam("password") String password, 
                               Model model, 
                               HttpSession session) {
        LoginResult result = userService.authenticate(email, password);
        
        if (!result.isSuccess()) {
            model.addAttribute("error", result.getMessage());
            return "login";
        }
        
        User loggedInUser = result.getUser();
        
        // Storing the user in session
        session.setAttribute("loggedInUser", loggedInUser);
        
        // Utilizing the refactored safe enum check method inside LoginResult
        if (result.isAdmin()) {
            return "redirect:/admin/dashboard";
        }
        
        return "redirect:/"; 
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session, Model model) {
        // FIXED: Aligned attribute key from "currentUser" to your active "loggedInUser" session key
        User user = (User) session.getAttribute("loggedInUser");
        String firstName = "";
        
        if (user != null && user.getName() != null) {
            firstName = user.getName().split(" ")[0];
        }
        
        model.addAttribute("firstName", firstName);
        
        // Invalidating cleans out all attributes safely in one command
        session.invalidate();
        return "logout";
    }
}
