package com.example.backend_lumiere.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Configuration
public class PasswordConfig {
    @Bean
    PasswordEncoder passwordEncoder() {
        BCryptPasswordEncoder bcrypt = new BCryptPasswordEncoder();
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence password) {
                return bcrypt.encode(password);
            }

            @Override
            public boolean matches(CharSequence password, String storedPassword) {
                if (password == null || storedPassword == null) return false;
                if (storedPassword.startsWith("$2")) return bcrypt.matches(password, storedPassword);
                return MessageDigest.isEqual(password.toString().getBytes(StandardCharsets.UTF_8),
                        storedPassword.getBytes(StandardCharsets.UTF_8));
            }
        };
    }
}
