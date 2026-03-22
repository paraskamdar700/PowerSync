package com.example.BuildingManagement.user.Security;

import com.example.BuildingManagement.user.Model.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

public class UserPrincipal implements UserDetails {

    private final Optional<User> user;

    public UserPrincipal(Optional<User> user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (user.isPresent()) {
            return Collections.singleton(
                    new SimpleGrantedAuthority("ROLE_"+user.get().getRole().name())
            );
        } else {
            return Collections.emptyList();
        }
    }

    public User getUser() {
        return this.user.orElse(null);
    }
    @Override
    public String getPassword() {
        return user.map(User::getPasswordHash).orElse(null);
    }

    @Override
    public String getUsername() {
        return user.map(User::getEmail).orElse(null);
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
