package com.example.qr_order.config;

import com.example.qr_order.entity.User;
import com.example.qr_order.enums.Role;
import com.example.qr_order.repository.UserRepo;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

public class DataInitializer implements CommandLineRunner {
    private final UserRepo user_repo;
    private final PasswordEncoder password_encoder;

    public DataInitializer(UserRepo user_repo,
                           PasswordEncoder password_encoder){
        this.user_repo = user_repo;
        this.password_encoder = password_encoder;
    }

    @Override
    public void run(String... args){
        seedUserIfNotExists("owner", "Admin", Role.OWNER, "Admin@123");
        seedUserIfNotExists("cashier", "Cashier", Role.CASHIER, "Cashier@123");
        seedUserIfNotExists("server", "Server", Role.SERVER, "Server@123");
    }


    private void seedUserIfNotExists(String user_name, String full_name, Role role, String raw_password) {
        boolean user_exists = user_repo.existsByUserName(user_name);
        if (user_exists) {
            return;
        }
        User user = new User();
        user.setUser_name(user_name);
        user.setFull_name(full_name);
        user.setRole(role);
        user.setPassword_hash(password_encoder.encode(raw_password));
        user.set_active(true);

        user_repo.save(user);
    }


}
