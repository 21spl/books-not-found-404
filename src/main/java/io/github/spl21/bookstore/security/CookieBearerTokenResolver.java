package io.github.spl21.bookstore.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

@Component
public class CookieBearerTokenResolver
        implements BearerTokenResolver {

    private static final String COOKIE_NAME = "JWT";

    @Override
    public String resolve(HttpServletRequest request) {

        // Public authentication endpoints don't need the existing JWT.
        String path = request.getServletPath();

        if (path.startsWith("/auth/")) {
            return null;
        }

        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        for (Cookie cookie : cookies) {
            if (COOKIE_NAME.equals(cookie.getName())) {
                String token = cookie.getValue();

                return (token == null || token.isBlank())
                        ? null
                        : token;
            }
        }

        return null;
    }
    
}