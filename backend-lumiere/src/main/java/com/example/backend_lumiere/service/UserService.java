package com.example.backend_lumiere.service;

import com.example.backend_lumiere.model.User;
import com.example.backend_lumiere.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // Get all users
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // Get user by ID
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // Create user
   public User createUser(User user) {
    if (user.getRole() == null || user.getRole().isBlank()) {
        user.setRole("USER");
    }
    return userRepository.save(user);
}

    //login user
    public User login(String email, String password) {
    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("Invalid email or password"));

    if (!user.getPassword().equals(password)) {
        throw new RuntimeException("Invalid email or password");
    }
    return user;
}

    // Update user
    public User updateUser(Long id, User newUser) {
        User existingUser = getUserById(id);

        existingUser.setName(newUser.getName());
        existingUser.setEmail(newUser.getEmail());
        existingUser.setPassword(newUser.getPassword());
        existingUser.setRole(newUser.getRole());

        return userRepository.save(existingUser);
    }

    // Delete user
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }
}