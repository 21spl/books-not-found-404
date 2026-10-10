package io.github.spl21.bookstore.security;

import java.time.Instant;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

@Service
public class JwtTokenService {

    private final JwtEncoder jwtEncoder;
    private final long expirationSeconds;

    // constructor
    public JwtTokenService(JwtEncoder jwtEncoder, @Value("${jwt.expiration-seconds}") long expirationSeconds) {

        this.jwtEncoder = jwtEncoder;
        this.expirationSeconds = expirationSeconds;
    }

    // generate Token
    public String generateToken(UserDetails userDetails){
        
        Instant now = Instant.now();

        List<String> roles = userDetails.getAuthorities()
                            .stream()
                            .map(GrantedAuthority::getAuthority)
                            .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                            .issuer("security-demo")
                            .issuedAt(now)
                            .expiresAt(now.plusSeconds(expirationSeconds))
                            .subject(userDetails.getUsername())
                            .claim("roles", roles)
                            .build();


        JwsHeader header = JwsHeader.with(MacAlgorithm.HS256).build();


        return jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
    
}
