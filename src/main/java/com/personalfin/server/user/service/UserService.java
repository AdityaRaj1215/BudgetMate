package com.personalfin.server.user.service;

import com.personalfin.server.security.validation.PasswordValidator;
import com.personalfin.server.user.model.Role;
import com.personalfin.server.user.model.User;
import com.personalfin.server.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PasswordValidator passwordValidator;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, PasswordValidator passwordValidator) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.passwordValidator = passwordValidator;
    }

    @Transactional
    public User createUser(String username, String email, String password) {
        // Validate password strength
        PasswordValidator.PasswordValidationResult validationResult = passwordValidator.validate(password);
        if (!validationResult.isValid()) {
            throw new IllegalArgumentException(validationResult.getErrorMessage());
        }

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists: " + username);
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists: " + email);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEnabled(true);
        
        Set<Role> roles = new HashSet<>();
        roles.add(Role.USER);
        user.setRoles(roles);

        return userRepository.save(user);
    }

    public User getUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found: " + id));
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsernameWithRoles(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
}






