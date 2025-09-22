package com.scm.services.impl;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.scm.entities.User;
import com.scm.helpers.AppConstants;
import com.scm.helpers.Helper;
import com.scm.helpers.ResourceNotFoundException;
import com.scm.repositories.UserRepo;
import com.scm.services.EmailService;
import com.scm.services.UserService;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepo userRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private EmailService emailService;

    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private Helper helper;

    @Override
    public User saveUser(User user) {
        try {
            String userId = UUID.randomUUID().toString();
            user.setUserId(userId);

            // encode password before saving
            user.setPassword(passwordEncoder.encode(user.getPassword()));

            // assign default role
            user.setRoleList(List.of(AppConstants.ROLE_USER));

            // enable user by default (otherwise login will fail)
            user.setEnabled(true);

            // generate email verification token
            String emailToken = UUID.randomUUID().toString();
            user.setEmailToken(emailToken);

            User savedUser = userRepo.save(user);

            String emailLink = helper.getLinkForEmailVerificatiton(emailToken);
            // emailService.sendEmail(savedUser.getEmail(), "Verify Account : Smart Contact Manager", emailLink);

            return savedUser;
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error saving user: {}", e.getMessage());
            return null;
        }
    }

    @Override
    public Optional<User> getUserById(String id) {
        return userRepo.findById(id);
    }

    @Override
    public Optional<User> updateUser(User user) {
        User user2 = userRepo.findById(user.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        user2.setName(user.getName());
        user2.setEmail(user.getEmail());

        // re-encode password only if changed
        if (user.getPassword() != null && !user.getPassword().isBlank()) {
            user2.setPassword(passwordEncoder.encode(user.getPassword()));
        }

        user2.setAbout(user.getAbout());
        user2.setPhoneNumber(user.getPhoneNumber());
        user2.setProfilePic(user.getProfilePic());
        user2.setEnabled(user.isEnabled());
        user2.setEmailVerified(user.isEmailVerified());
        user2.setPhoneVerified(user.isPhoneVerified());
        user2.setProvider(user.getProvider());
        user2.setProviderUserId(user.getProviderUserId());

        User save = userRepo.save(user2);
        return Optional.ofNullable(save);
    }

    @Override
    public void deleteUser(String id) {
        User user2 = userRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        userRepo.delete(user2);
    }

    @Override
    public boolean isUserExist(String userId) {
        return userRepo.findById(userId).isPresent();
    }

    @Override
    public boolean isUserExistByEmail(String email) {
        return userRepo.findByEmail(email).isPresent();
    }

    @Override
    public List<User> getAllUsers() {
        return userRepo.findAll();
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepo.findByEmail(email).orElse(null);
    }
}
