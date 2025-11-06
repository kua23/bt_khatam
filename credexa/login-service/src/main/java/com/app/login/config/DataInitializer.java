package com.app.login.config;

import java.util.HashSet;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.app.login.entity.BankConfiguration;
import com.app.login.entity.Role;
import com.app.login.entity.User;
import com.app.login.repository.BankConfigurationRepository;
import com.app.login.repository.RoleRepository;
import com.app.login.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Data initializer to create default roles, admin user, and bank configuration
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final BankConfigurationRepository bankConfigRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        log.info("Initializing default data...");

        // Create default roles
        createDefaultRoles();

        // Create default admin user
        createDefaultAdminUser();

        // Create default test users
        createDefaultTestUsers();

        // Create default bank configuration
        createDefaultBankConfiguration();

        log.info("Default data initialization completed");
    }

    private void createDefaultTestUsers() {
        log.info("Creating default test users...");

        // Create manager user
        createUserIfNotExists("manager1", "Manager@123", "manager1@example.com", 
            "9876543212", Role.RoleName.ROLE_MANAGER);

        // Create customer user
        createUserIfNotExists("customer1", "Customer@123", "customer1@example.com", 
            "9876543213", Role.RoleName.ROLE_CUSTOMER);

        log.info("Default test users created successfully");
    }

    private void createUserIfNotExists(String username, String password, String email, 
                                     String mobile, Role.RoleName... roleNames) {
        if (!userRepository.existsByUsername(username)) {
            User user = User.builder()
                    .username(username)
                    .password(passwordEncoder.encode(password))
                    .email(email)
                    .mobileNumber(mobile)
                    .preferredLanguage("en")
                    .preferredCurrency("USD")
                    .active(true)
                    .accountLocked(false)
                    .failedLoginAttempts(0)
                    .createdBy("SYSTEM")
                    .roles(new HashSet<>())
                    .build();

            // Add roles
            if (roleNames.length > 0) {
                for (Role.RoleName roleName : roleNames) {
                    Role role = roleRepository.findByName(roleName)
                            .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                    user.getRoles().add(role);
                }
            } else {
                // If no role specified, assign CUSTOMER role by default
                Role customerRole = roleRepository.findByName(Role.RoleName.ROLE_CUSTOMER)
                        .orElseThrow(() -> new RuntimeException("ROLE_CUSTOMER not found"));
                user.getRoles().add(customerRole);
            }

            user = userRepository.save(user);
            
            log.info("Created user: {} with roles: {}", username, 
                    user.getRoles().stream()
                        .map(role -> role.getName().name())
                        .collect(Collectors.joining(", ")));
        } else {
            // Update roles for existing user
            User existingUser = userRepository.findByUsername(username)
                    .orElseThrow(() -> new RuntimeException("User not found: " + username));
            
            for (Role.RoleName roleName : roleNames) {
                Role role = roleRepository.findByName(roleName)
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleName));
                if (!existingUser.getRoles().contains(role)) {
                    existingUser.getRoles().add(role);
                }
            }

            userRepository.save(existingUser);
            log.info("Updated roles for existing user: {}", username);
        }
    }

    private void createDefaultRoles() {
        log.info("Creating default roles...");
        for (Role.RoleName roleName : Role.RoleName.values()) {
            if (!roleRepository.existsByName(roleName)) {
                Role role = Role.builder()
                        .name(roleName)
                        .description("Default role: " + roleName.name())
                        .build();
                role = roleRepository.save(role);
                log.info("Created role: {} with ID: {}", roleName, role.getId());
            } else {
                log.info("Role already exists: {}", roleName);
            }
        }
        log.info("Total roles in database: {}", roleRepository.count());
    }

    private void createDefaultAdminUser() {
        String adminUsername = "admin";
        log.info("Checking for admin user...");
        
        // First check if admin exists
        User existingAdmin = userRepository.findByUsername(adminUsername).orElse(null);
        if (existingAdmin != null) {
            log.info("Admin user exists. Verifying roles...");
            // Ensure admin has ROLE_ADMIN
            Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));
            
            if (!existingAdmin.getRoles().contains(adminRole)) {
                log.info("Adding missing ROLE_ADMIN to existing admin user");
                existingAdmin.getRoles().add(adminRole);
                userRepository.save(existingAdmin);
            }
            return;
        }

        log.info("Creating new admin user...");
        Role adminRole = roleRepository.findByName(Role.RoleName.ROLE_ADMIN)
                .orElseThrow(() -> new RuntimeException("Admin role not found"));

        User admin = User.builder()
                .username(adminUsername)
                .password(passwordEncoder.encode("Admin@123"))
                .email("admin@credexa.com")
                .mobileNumber("9999999999")
                .preferredLanguage("en")
                .preferredCurrency("USD")
                .active(true)
                .accountLocked(false)
                .failedLoginAttempts(0)
                .createdBy("SYSTEM")
                .roles(new HashSet<>())
                .build();

        admin.getRoles().add(adminRole);
        admin = userRepository.save(admin);
            
        log.info("========================================");
        log.info("Admin User Created/Updated:");
        log.info("Username: {}", admin.getUsername());
        log.info("Email: {}", admin.getEmail());
        log.info("Roles: {}", admin.getRoles().stream()
                .map(role -> role.getName().name())
                .collect(Collectors.joining(", ")));
        log.info("========================================");
    }

    private void createDefaultBankConfiguration() {
        if (bankConfigRepository.findByActiveTrue().isEmpty()) {
            BankConfiguration config = BankConfiguration.builder()
                    .bankName("Credexa Bank")
                    .logoUrl("/assets/logo.png")
                    .defaultLanguage("en")
                    .defaultCurrency("USD")
                    .currencyDecimalPlaces(2)
                    .active(true)
                    .build();
            
            bankConfigRepository.save(config);
            log.info("Created default bank configuration");
        }
    }
}
