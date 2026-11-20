package com.example.backend_lumiere.service;

import com.example.backend_lumiere.model.User;
import com.example.backend_lumiere.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    // Create user
    public User createUser(String name, String email, String password) {
        return createManagedUser(name, email, password, "CUSTOMER");
    }

    public User createManagedUser(String name, String email, String password, String role) {
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        if (userRepository.findByEmailIgnoreCase(normalizedEmail).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        User user = new User();
        user.setName(name.trim());
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(normalizeRole(role));
        user.setActive(true);
        return userRepository.save(user);
    }

    public User changeStatus(Long id, boolean active) {
        User user = getUserById(id);
        user.setActive(active);
        return userRepository.save(user);
    }

    public User changeRole(Long id, String role) {
        User user = getUserById(id);
        user.setRole(normalizeRole(role));
        return userRepository.save(user);
    }

    private String normalizeRole(String role) {
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if (normalized.equals("USER")) normalized = "CUSTOMER";
        if (!normalized.equals("ADMIN") && !normalized.equals("CUSTOMER")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be ADMIN or CUSTOMER");
        }
        return normalized;
    }

    //login user
    public User login(String email, String password) {
    User user = userRepository.findByEmailIgnoreCase(email == null ? "" : email.trim())
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));

    if (!user.isActive() || !passwordEncoder.matches(password, user.getPassword())) {
        throw new RuntimeException("Invalid email or password");
    }
    return user;
}

    // Update user
    public User updateUser(Long id, User newUser) {
        User existingUser = getUserById(id);

        existingUser.setName(newUser.getName());
        existingUser.setEmail(newUser.getEmail().trim().toLowerCase(Locale.ROOT));
        if (newUser.getPassword() != null && !newUser.getPassword().isBlank()) {
            existingUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        }
        existingUser.setRole(normalizeRole(newUser.getRole()));

        return userRepository.save(existingUser);
    }

    // Delete user
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}
