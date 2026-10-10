package io.github.spl21.bookstore.security;

import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.spl21.bookstore.dto.LoginRequestDto;
import io.github.spl21.bookstore.dto.RegisterRequestDto;
import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.enums.Role;
import io.github.spl21.bookstore.exception.UserAlreadyExistsException;
import io.github.spl21.bookstore.repository.UserRepository;


@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenService jwtTokenService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtTokenService jwtTokenService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public void register(RegisterRequestDto request) {

        // STEP 1: CHECK IF USER ALREADY EXISTS
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new UserAlreadyExistsException(
                    "username", "This username is already taken");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new UserAlreadyExistsException(
                    "email", "An account with this email already exists");
        }

        // STEP 2: CREATE USER
        User user = new User();

        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());

        // Never store the submitted raw password.
        user.setPassword(
                passwordEncoder.encode(request.getPassword()));

        // Public registration must not grant ADMIN privileges.
        user.setRoles(new HashSet<>(Set.of(Role.CUSTOMER)));

        userRepository.save(user);
    }

    public String login(LoginRequestDto request)
            throws AuthenticationException {

        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()
                        )
                );
        // UsernamePasswordAuthenticationToken just represents the submitted credentials
        // It doesn't mean that the user is authenticated

        // Internally authentication Manager delegates to DaoAuthenticationProvider which uses
        // CustomUserDetailsService and PasswordEncoder to authenticate the credentials

        // If authentication fails, the manager throws AuthenticationException, execution stops

        // If authentication suceeds, the manager returns an authenticated "Authentication" object


        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();
        // authentication.getPrincipal() returns the principal
        //  (principal represents the authenticated user)
        // here principal is a SecurityUser object (which is of type UserDetails, 
        // because SecurityUser implements UserDetails interface)

        return jwtTokenService.generateToken(userDetails);
        // pass the UserDetails object (SecurityUser) to jwtTokenService
    }
}