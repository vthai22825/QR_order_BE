package com.example.qr_order.config;

import com.example.qr_order.entity.Category;
import com.example.qr_order.entity.User;
import com.example.qr_order.enums.Role;
import com.example.qr_order.repository.CategoryRepo;
import com.example.qr_order.repository.UserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DataInitializer implements CommandLineRunner {
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;
    private final CategoryRepo categoryRepo;

    public DataInitializer(UserRepo userRepo,
            PasswordEncoder passwordEncoder, CategoryRepo categoryRepo) {
        this.userRepo = userRepo;
        this.passwordEncoder = passwordEncoder;
        this.categoryRepo = categoryRepo;
    }

    @Override
    public void run(String... args) {
        seedUserIfNotExists("owner", "Admin", Role.OWNER, "Admin@123");
        seedUserIfNotExists("cashier", "Cashier", Role.CASHIER, "Cashier@123");
        seedUserIfNotExists("server", "Server", Role.SERVER, "Server@123");
        seedUserIfNotExists("chef", "Chef", Role.CHEF, "Chef@123");

        seedCategories();
    }

    private void seedCategories() {
        List<String> categoryNames = List.of(
                "Chicken", "Pizza", "Steak", "Noodle",
                "Burger", "Salad", "Drink", "Dessert");
        for (String name : categoryNames) {
            if (!categoryRepo.existsByName(name)) {
                Category category = new Category();
                category.setName(name);
                category.setDescription(name + " category");
                categoryRepo.save(category);
            }
        }
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
