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

        // ask the servlet request for the cookies
        Cookie[] cookies = request.getCookies();

        if (cookies == null) {
            return null;
        }

        // among all the cookies, search for the cookie named JWT
        for (Cookie cookie : cookies) {

            if (COOKIE_NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }

        return null;
    }
}