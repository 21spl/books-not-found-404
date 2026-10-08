package io.github.spl21.bookstore.security;

import java.util.Collection;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import io.github.spl21.bookstore.entity.User;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter 
public class SecurityUser implements UserDetails{
    
    private final User user;

    // constructor injection
    public SecurityUser(User user) {
        this.user = user;
    }


    @Override
    public String getUsername(){
        return user.getUsername();
    }

    @Override
    public String getPassword(){
        return user.getPassword();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                // Turn Role enum (e.g., ADMIN -> "ROLE_ADMIN") into a SimpleGrantedAuthority
                .map(roleEnum -> new SimpleGrantedAuthority("ROLE_" + roleEnum.name()))
                .collect(Collectors.toList());
    }


    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
