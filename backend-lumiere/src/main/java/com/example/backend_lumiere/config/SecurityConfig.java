package com.example.backend_lumiere.config;

import com.example.backend_lumiere.repository.UserRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfig {
    @Bean
    SecurityFilterChain securityFilterChain(org.springframework.security.config.annotation.web.builders.HttpSecurity http,
                                           UserRepository users) throws Exception {
        AuthorizationManager<RequestAuthorizationContext> adminAccess = (authentication, context) -> {
            var current = authentication.get();
            boolean allowed = current != null && current.isAuthenticated()
                    && users.findByEmail(current.getName())
                    .map(user -> user.isActive() && "ADMIN".equalsIgnoreCase(user.getRole())).orElse(false);
            return new AuthorizationDecision(allowed);
        };
        return http
                .cors(withDefaults())
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .securityContext(context -> context.securityContextRepository(new HttpSessionSecurityContextRepository()))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/api/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/users", "/api/users/**").access(adminAccess)
                        .requestMatchers(HttpMethod.POST, "/api/users/admin").access(adminAccess)
                        .requestMatchers(HttpMethod.PATCH, "/api/users/*/role").access(adminAccess)
                        .requestMatchers(HttpMethod.PATCH, "/api/users/*/status").access(adminAccess)
                        .requestMatchers(HttpMethod.PUT, "/api/users/**").access(adminAccess)
                        .requestMatchers(HttpMethod.DELETE, "/api/users/**").access(adminAccess)
                        .requestMatchers(HttpMethod.POST, "/api/users").permitAll()
                        .anyRequest().permitAll())
                .httpBasic(withDefaults())
                .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, exception) ->
                        response.sendError(HttpStatus.UNAUTHORIZED.value())))
                .build();
    }

    @Bean
    UserDetailsService userDetailsService(UserRepository users) {
        return email -> {
            var user = users.findByEmail(email)
                    .orElseThrow(() -> new UsernameNotFoundException("Invalid credentials"));
            String role = user.getRole() == null ? "CUSTOMER" : user.getRole().toUpperCase(Locale.ROOT);
            if (role.equals("USER")) role = "CUSTOMER";
            return org.springframework.security.core.userdetails.User.withUsername(user.getEmail())
                    .password(user.getPassword())
                    .disabled(!user.isActive())
                    .roles(role)
                    .build();
        };
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return bcrypt.encode(rawPassword);
            }

            @Override
            public boolean matches(CharSequence rawPassword, String storedPassword) {
                if (storedPassword == null || rawPassword == null) return false;
                if (storedPassword.startsWith("$2")) return bcrypt.matches(rawPassword, storedPassword);
                return MessageDigest.isEqual(rawPassword.toString().getBytes(StandardCharsets.UTF_8),
                        storedPassword.getBytes(StandardCharsets.UTF_8));
            }
        };
    }
}
