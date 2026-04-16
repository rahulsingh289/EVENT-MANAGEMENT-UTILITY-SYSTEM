package com.example.eventmanagement;

import com.example.eventmanagement.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class EventManagementApplication {

    public static void main(String[] args) {
        SpringApplication.run(EventManagementApplication.class, args);
    }

    // Runs once on startup — promotes the configured username to ROLE_ADMIN
    // Set app.init-admin-username in application.properties, then remove it after first run
    @Bean
    CommandLineRunner promoteAdmin(
            UserRepository userRepository,
            @Value("${app.init-admin-username:}") String adminUsername) {
        return args -> {
            if (adminUsername.isBlank()) return;
            userRepository.findByUsername(adminUsername).ifPresent(user -> {
                if (!"ROLE_ADMIN".equals(user.getRole())) {
                    user.setRole("ROLE_ADMIN");
                    userRepository.save(user);
                    System.out.println("[Init] Promoted '" + adminUsername + "' to ROLE_ADMIN");
                } else {
                    System.out.println("[Init] '" + adminUsername + "' is already ROLE_ADMIN");
                }
            });
        };
    }
}
