package com.example.qr_order.service;


import com.example.qr_order.entity.BlackListToken;
import com.example.qr_order.repository.BlackListTokenRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class BlackListTokenService {

    private final BlackListTokenRepo black_list_token_repo;

    public void black_list(String token, LocalDateTime expires_at){
        if (!black_list_token_repo.existsByToken(token)){
            black_list_token_repo.save(new BlackListToken(token, expires_at, null));
        }
    }

    public boolean is_black_list(String token){
        return black_list_token_repo.existsByToken(token);
    }

}
