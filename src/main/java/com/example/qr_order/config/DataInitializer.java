package com.example.qr_order.config;

import com.example.qr_order.entity.User;
import com.example.qr_order.enums.Role;
import com.example.qr_order.repository.UserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepo userRepo,
            PasswordEncoder passwordEncoder) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedUserIfNotExists("owner", "Admin", Role.OWNER, "Admin@123");
        seedUserIfNotExists("cashier", "Cashier", Role.CASHIER, "Cashier@123");
        seedUserIfNotExists("server", "Server", Role.SERVER, "Server@123");
        seedUserIfNotExists("chef", "Chef", Role.CHEF, "Chef@123");
    }

    private void seedUserIfNotExists(String userName, String fullName, Role role, String rawPassword) {
        boolean userExists = userRepo.existsByUserName(userName);
        if (userExists) {
            return;
        }
        User user = new User();
        user.setUserName(userName);
        user.setFullName(fullName);
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(rawPassword));
        user.setActive(true);

        userRepo.save(user);
    }

}
