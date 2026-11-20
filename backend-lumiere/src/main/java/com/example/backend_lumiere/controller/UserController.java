package com.example.backend_lumiere.controller;

import com.example.backend_lumiere.model.User;
import com.example.backend_lumiere.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    // Constructor injection
    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET: Get all users
    @GetMapping
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    // GET: Get user by ID
    @GetMapping("/{id}")
    public User getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    // POST: Create a new user
    @PostMapping
    public User createUser(@RequestBody User user) {
        return userService.createUser(user);
    }

    public record ManagedUserRequest(@NotBlank String name, @Email @NotBlank String email,
                                     @NotBlank @Size(min = 8) String password, @NotBlank String role) {}

    public record RoleRequest(@NotBlank String role) {}

    public record StatusRequest(@NotNull Boolean active) {}

    @PostMapping("/admin")
    @ResponseStatus(HttpStatus.CREATED)
    public User createManagedUser(@Valid @RequestBody ManagedUserRequest request) {
        return userService.createManagedUser(request.name(), request.email(), request.password(), request.role());
    }

    @PatchMapping("/{id}/role")
    public User changeRole(@PathVariable Long id, @Valid @RequestBody RoleRequest request) {
        return userService.changeRole(id, request.role());
    }

    @PatchMapping("/{id}/status")
    public User changeStatus(@PathVariable Long id, @Valid @RequestBody StatusRequest request) {
        return userService.changeStatus(id, request.active());
    }

    // PUT: Update user
    @PutMapping("/{id}")
    public User updateUser(
            @PathVariable Long id,
            @RequestBody User user) {
        return userService.updateUser(id, user);
    }

    // DELETE: Delete user
    @DeleteMapping("/{id}")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "User deleted successfully";
    }
}
