package com.example.backend_lumiere.service;

import com.example.backend_lumiere.model.User;
import com.example.backend_lumiere.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
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

    // Get user by email
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    // Create user (public registration, always a CUSTOMER)
    public User createUser(User user) {
        String email = normalizeEmail(user.getEmail());
        if (userRepository.findByEmail(email).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
        }
        user.setId(null);
        user.setEmail(email);
        user.setRole("CUSTOMER");
        user.setActive(true);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        return userRepository.save(user);
    }

    // Create user from the admin side, with a chosen role
    public User createManagedUser(String name, String email, String password, String role) {
        String normalizedEmail = normalizeEmail(email);
        if (userRepository.findByEmail(normalizedEmail).isPresent()) {
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

    // Login user
    public User login(String email, String password) {
        User user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));

        if (!user.isActive() || !passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return user;
    }

    // Update user (role and active are changed only through changeRole / changeStatus)
    public User updateUser(Long id, User newUser) {
        User existing = getUserById(id);

        if (newUser.getName() != null) {
            existing.setName(newUser.getName().trim());
        }

        if (newUser.getEmail() != null) {
            String email = normalizeEmail(newUser.getEmail());
            userRepository.findByEmail(email).ifPresent(other -> {
                if (!other.getId().equals(id)) {
                    throw new ResponseStatusException(HttpStatus.CONFLICT, "Email is already registered");
                }
            });
            existing.setEmail(email);
        }

        if (newUser.getPassword() != null && !newUser.getPassword().isBlank()) {
            existing.setPassword(passwordEncoder.encode(newUser.getPassword()));
        }

        return userRepository.save(existing);
    }

    // Delete user
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        userRepository.deleteById(id);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeRole(String role) {
        String normalized = role.trim().toUpperCase(Locale.ROOT);
        if (normalized.equals("USER")) normalized = "CUSTOMER";
        if (!normalized.equals("ADMIN") && !normalized.equals("CUSTOMER")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be ADMIN or CUSTOMER");
        }
        return normalized;
    }
}
