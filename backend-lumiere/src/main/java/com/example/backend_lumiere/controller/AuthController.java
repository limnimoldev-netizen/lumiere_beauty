package com.example.backend_lumiere.controller;

import com.example.backend_lumiere.model.User;
import com.example.backend_lumiere.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final UserDetailsService userDetailsService;
    private final HttpSessionSecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthController(UserService userService, UserDetailsService userDetailsService) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody User request, HttpServletRequest servletRequest, HttpServletResponse servletResponse) {
        try {
            User user = userService.login(request.getEmail(), request.getPassword());
            var details = userDetailsService.loadUserByUsername(user.getEmail());
            var authentication = UsernamePasswordAuthenticationToken.authenticated(details, null, details.getAuthorities());
            var context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            if (servletRequest.getSession(false) != null) servletRequest.changeSessionId();
            securityContextRepository.saveContext(context, servletRequest, servletResponse);
            return ResponseEntity.ok(user);
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(Map.of("message", e.getMessage()));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        var session = request.getSession(false);
        if (session != null) session.invalidate();
        SecurityContextHolder.clearContext();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<User> currentUser(Principal principal) {
        if (principal == null) return ResponseEntity.status(401).build();
        User user = userService.getUserByEmail(principal.getName());
        if (!user.isActive()) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(user);
    }
}
