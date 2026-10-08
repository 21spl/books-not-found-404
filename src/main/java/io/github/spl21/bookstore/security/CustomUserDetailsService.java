package io.github.spl21.bookstore.security;

import java.util.Optional;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import io.github.spl21.bookstore.entity.User;
import io.github.spl21.bookstore.repository.UserRepository;

@Service 
public class CustomUserDetailsService implements UserDetailsService{

    private final UserRepository userRepository;
    
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException{
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isEmpty()){
            throw new UsernameNotFoundException("User not found");
        }
        return new SecurityUser(user.get());
        
    } 
}
