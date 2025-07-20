package com.eagle.service;

import com.eagle.entity.User;
import com.eagle.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class UserService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String userId) throws UsernameNotFoundException {
        return userRepository.findByUserId(userId)
                .map(this::toUserDetails)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userId));
    }

    public Optional<User> findByUserId(String userId) throws UsernameNotFoundException {
        final String tokenUserId = SecurityContextHolder.getContext().getAuthentication().getName();
        if (Objects.equals(tokenUserId, userId)) {
            return userRepository.findByUserId(userId);
        } else {
            throw new AccessDeniedException("User not allowed to access this resource");
        }
    }

    private UserDetails toUserDetails(User user) {
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUserId())
                .password(user.getPasswordHash())
                .build();
    }
}
