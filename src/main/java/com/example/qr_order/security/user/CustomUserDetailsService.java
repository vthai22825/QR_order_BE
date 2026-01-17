package com.example.qr_order.security.user;

import com.example.qr_order.entity.User;
import com.example.qr_order.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepo userRepo;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // Sửa findByEmail -> findByUserName
        User user = userRepo.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));

        return CustomUserDetails.create(user);
    }

    // Hàm này hỗ trợ lấy User bằng ID (cho JWT Filter sau này)
    @Transactional
    public UserDetails loadUserById(Long id) { // Sửa String -> Long
        User user = userRepo.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));

        return CustomUserDetails.create(user);
    }
}