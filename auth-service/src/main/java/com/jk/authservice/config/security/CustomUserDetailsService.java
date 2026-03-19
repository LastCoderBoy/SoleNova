package com.jk.authservice.config.security;

import com.jk.authservice.entity.User;
import com.jk.authservice.entity.UserPrincipal;
import com.jk.authservice.repository.UserRepository;
import com.jk.commonlibrary.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Transactional
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // Reset the failed login attempts if the account locked time is expired
        if(user.getAccountLocked()){
            if(user.getLockedUntil() != null && LocalDateTime.now().isAfter(user.getLockedUntil())){
                user.resetFailedLoginAttempts();
                userRepository.save(user);
            }
        }

        return UserPrincipal.create(user);
    }
}
