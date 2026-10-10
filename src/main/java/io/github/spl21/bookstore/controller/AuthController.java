package io.github.spl21.bookstore.controller;

import java.time.Duration;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import io.github.spl21.bookstore.dto.LoginRequestDto;
import io.github.spl21.bookstore.dto.RegisterRequestDto;
import io.github.spl21.bookstore.exception.UserAlreadyExistsException;
import io.github.spl21.bookstore.security.AuthService;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private static final String JWT_COOKIE_NAME = "JWT";

    private final AuthService authService;
    private final JwtDecoder jwtDecoder;
    private final long jwtExpirationSeconds;
    private final boolean secureCookie;

    public AuthController(
            AuthService authService,
            JwtDecoder jwtDecoder,
            @Value("${jwt.expiration-seconds}") long jwtExpirationSeconds,
            @Value("${app.cookie.secure:true}") boolean secureCookie) {

        this.authService = authService;
        this.jwtExpirationSeconds = jwtExpirationSeconds;
        this.jwtDecoder = jwtDecoder;
        this.secureCookie = secureCookie;
    }

    // ---------------- REGISTER ----------------

    @GetMapping("/register")
    public String showRegisterForm(Model model) {

        model.addAttribute(
                "registerRequest", new RegisterRequestDto());

        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest")
            RegisterRequestDto request,
            BindingResult bindingResult,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "auth/register";
        }

        try {
            authService.register(request);
        } catch (UserAlreadyExistsException exception) {

            bindingResult.rejectValue(
                    exception.getField(),
                    "duplicate",
                    exception.getMessage()
            );

            return "auth/register";
        }

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Registration successful. You can now log in."
        );

        return "redirect:/auth/login";
    }

    // ---------------- LOGIN ----------------

    @GetMapping("/login")
    public String showLoginForm(Model model) {

        model.addAttribute(
                "loginRequest", new LoginRequestDto());

        return "auth/login";
    }

    @PostMapping("/login")
    public String login(
            @Valid @ModelAttribute("loginRequest")
            LoginRequestDto request,
            BindingResult bindingResult,
            HttpServletResponse response,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "auth/login";
        }

        try {
            String token = authService.login(request);

            System.out.println("Secure cookie enabled: " + secureCookie);

            ResponseCookie jwtCookie = ResponseCookie
                    .from(JWT_COOKIE_NAME, token)
                    .httpOnly(true)
                    .secure(secureCookie)
                    .sameSite("Lax")
                    .path("/")
                    .maxAge(Duration.ofSeconds(jwtExpirationSeconds))
                    .build();

            response.addHeader(
                    "Set-Cookie", jwtCookie.toString());

            Jwt jwt = jwtDecoder.decode(token);

            if (jwt.getClaimAsStringList("roles").contains("ROLE_ADMIN")) {
                return "redirect:/admin/dashboard";
            }

            return "redirect:/profile";

        } catch (AuthenticationException exception) {

            // Avoid revealing whether the username or password was wrong.
            model.addAttribute(
                    "loginError",
                    "Invalid username or password");

            return "auth/login";
        }
    }

    // ---------------- LOGOUT ----------------
    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {

        ResponseCookie expiredCookie = ResponseCookie
                .from(JWT_COOKIE_NAME, "")
                .httpOnly(true)
                .secure(secureCookie)
                .sameSite("Lax")
                .path("/")
                .maxAge(Duration.ZERO)
                .build();

        response.addHeader("Set-Cookie", expiredCookie.toString());

        return "auth/logout";
    }
}