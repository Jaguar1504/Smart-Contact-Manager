package com.scm.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.scm.entities.User;
import com.scm.repositories.UserRepo;

@Service
public class SecurityCustomUserDetailService implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(SecurityCustomUserDetailService.class);

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.info("Trying to load user by email: {}", username);

        User user = userRepo.findByEmail(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email : " + username));

        if (!user.isEnabled()) {
            logger.warn("User found but not enabled: {}", username);
            throw new UsernameNotFoundException("Account is not enabled. Please verify your email first.");
        }

        logger.info("User loaded successfully: {}", username);
        return user; // ✅ your User entity implements UserDetails, so this works
    }
}
