package io.github.spl21.bookstore.config;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import io.github.spl21.bookstore.security.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final BearerTokenResolver cookieBearerTokenResolver;

    public SecurityConfig(
            CustomUserDetailsService customUserDetailsService,
            BearerTokenResolver cookieBearerTokenResolver) {

        this.customUserDetailsService = customUserDetailsService;
        this.cookieBearerTokenResolver = cookieBearerTokenResolver;
    }

    // 1. Password hashing

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // 2. Username/password authentication

    @Bean
    public AuthenticationProvider authenticationProvider() {

        DaoAuthenticationProvider authProvider =
                new DaoAuthenticationProvider(customUserDetailsService);

        authProvider.setPasswordEncoder(passwordEncoder());

        return authProvider;
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider());
    }

    // 3. Shared JWT signing and verification key

    @Bean
    public SecretKey jwtSecretKey(
            @Value("${jwt.secret}") String secret) {

        byte[] keyBytes =
                secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalArgumentException(
                    "JWT secret must contain at least 32 bytes for HS256");
        }

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    // 4. JWT encoder: signs tokens

    @Bean
    public JwtEncoder jwtEncoder(SecretKey secretKey) {

        return new NimbusJwtEncoder(
                new ImmutableSecret<>(secretKey));
    }

    // 5. JWT decoder: verifies tokens

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey secretKey) {

        NimbusJwtDecoder decoder = NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        decoder.setJwtValidator(
                JwtValidators.createDefaultWithIssuer("security-demo"));

        return decoder;
    }

    // 6. Convert the custom "roles" claim into authorities

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {

        JwtGrantedAuthoritiesConverter authoritiesConverter =
                new JwtGrantedAuthoritiesConverter();

        authoritiesConverter.setAuthoritiesClaimName("roles");

        // JWT already contains ROLE_CUSTOMER / ROLE_ADMIN.
        // Do not prepend ROLE_ a second time.
        authoritiesConverter.setAuthorityPrefix("");

        JwtAuthenticationConverter authenticationConverter =
                new JwtAuthenticationConverter();

        authenticationConverter.setJwtGrantedAuthoritiesConverter(
                authoritiesConverter);

        return authenticationConverter;
    }

    // 7. HTTP security rules and request-processing pipeline

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            CsrfTokenRepository csrfTokenRepository,
            JwtAuthenticationConverter jwtAuthenticationConverter)
            throws Exception {

        http
            .authorizeHttpRequests(authorize -> authorize

                // Public pages and resources
                .requestMatchers(
                        "/",
                        "/auth/**",
                        "/search/*",
                        "/explore_genre",
                        "/books/*",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/error"
                ).permitAll()

                // Example role-specific URL areas
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/customer/**", "/profile/*", "/cart/**", "/checkout/**").hasRole("CUSTOMER")

                // Both customers and admins can access user pages
                .requestMatchers("/user/**")
                    .hasAnyRole("CUSTOMER", "ADMIN")

                // Protect anything not explicitly made public
                .anyRequest().authenticated()
            )

            // CSRF protection remains enabled for browser forms.
            // Do not disable it merely because authentication uses JWT.
            .csrf(csrf -> csrf
                .csrfTokenRepository(csrfTokenRepository)
            )
            // Redirect unauthenticated browser requests to the login page.
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint(
                    (request, response, exception) ->
                        response.sendRedirect("/auth/login")
                )
            )

            // Read the JWT from the cookie instead of requiring
            // an Authorization: Bearer header.
            .oauth2ResourceServer(oauth2 -> oauth2
                .bearerTokenResolver(cookieBearerTokenResolver)
                .jwt(jwt -> jwt
                    .jwtAuthenticationConverter(
                            jwtAuthenticationConverter)
                )
            );

        return http.build();
    }


    @Bean
    public CsrfTokenRepository csrfTokenRepository() {
        HttpSessionCsrfTokenRepository repository =
                new HttpSessionCsrfTokenRepository();

        repository.setHeaderName("X-CSRF-TOKEN");

        return repository;
    }
}