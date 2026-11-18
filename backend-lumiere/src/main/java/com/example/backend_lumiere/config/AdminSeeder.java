package com.example.backend_lumiere.config;

import com.example.backend_lumiere.model.User;
import com.example.backend_lumiere.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminSeeder {

    @Bean
    CommandLineRunner seedAdmin(UserRepository repo, PasswordEncoder passwordEncoder) {
        return args -> {
            if (repo.findByEmail("admin@gmail.com").isEmpty()) {
                User admin = new User();
                admin.setName("Admin");
                admin.setEmail("admin@gmail.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");
                repo.save(admin);
            }
        };
    }
}
