package com.smartagriculture;

import com.smartagriculture.entity.User;
import com.smartagriculture.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class SmartAgricultureApplication {

    private static final Logger log = LoggerFactory.getLogger(SmartAgricultureApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(SmartAgricultureApplication.class, args);
    }

    /**
     * Optional secure initial administrator provisioning via environment variables.
     * Only activates if both ADMIN_INIT_EMAIL and ADMIN_INIT_PASSWORD are provided
     * AND no administrator already exists in the database.
     */
    @Bean
    public CommandLineRunner adminBootstrapRunner(UserRepository userRepository,
                                                  PasswordEncoder passwordEncoder,
                                                  @Value("${ADMIN_INIT_EMAIL:}") String initEmail,
                                                  @Value("${ADMIN_INIT_PASSWORD:}") String initPassword,
                                                  @Value("${ADMIN_INIT_NAME:System Administrator}") String initName) {
        return args -> {
            if (!initEmail.isBlank() && !initPassword.isBlank()) {
                long adminCount = userRepository.countByRole("ROLE_ADMIN");
                if (adminCount == 0) {
                    String cleanEmail = initEmail.trim().toLowerCase();
                    if (!userRepository.existsByEmailIgnoreCase(cleanEmail)) {
                        User admin = new User();
                        admin.setFullName(initName.trim());
                        admin.setEmail(cleanEmail);
                        admin.setPasswordHash(passwordEncoder.encode(initPassword));
                        admin.setRole("ROLE_ADMIN");
                        admin.setPreferredLanguage("en");
                        userRepository.save(admin);
                        log.info("Secure initial administrator provisioned for email: {}", cleanEmail);
                    }
                } else {
                    log.info("Administrator accounts already exist. Bootstrap provisioning permanently disabled.");
                }
            }
        };
    }
}
