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
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;

import com.nimbusds.jose.jwk.source.ImmutableSecret;

import io.github.spl21.bookstore.security.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    
    public SecurityConfig(CustomUserDetailsService customUserDetailsService) {
        this.customUserDetailsService = customUserDetailsService;
    }

    // 1. Password Encoder
    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    // 2. Authentication Provider: connects db lookup and password checking
    @Bean 
    public AuthenticationProvider authenticationProvider(){
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider(customUserDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }


    // 3. Authentication Manager
    @Bean
    public AuthenticationManager authenticationManager(){
        return new ProviderManager(authenticationProvider());
    }

    

   // JWT RELATED CONFIGURATIONS ============================================================

    /*
    * --------------------------------------------------
    * SECRET KEY
    * --------------------------------------------------
    */
    @Bean 
    public SecretKey jwtSecretKey(@Value("${jwt.secret}") String secret){
        
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        
        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    /*
     * --------------------------------------------------
     * JWT ENCODER
     * --------------------------------------------------
     */

    @Bean
    public JwtEncoder jwtEncoder(
            SecretKey secretKey) {

        return new NimbusJwtEncoder(new ImmutableSecret<>(secretKey));
    }

    /*
     * --------------------------------------------------
     * JWT DECODER
     * --------------------------------------------------
     */

    @Bean
    public JwtDecoder jwtDecoder(
            SecretKey secretKey) {

        return NimbusJwtDecoder
                .withSecretKey(secretKey)
                .macAlgorithm(MacAlgorithm.HS256)
                .build();
    }

    

     
}

