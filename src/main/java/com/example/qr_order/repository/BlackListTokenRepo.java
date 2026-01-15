package com.example.qr_order.repository;

import com.example.qr_order.entity.BlackListToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BlackListTokenRepo extends JpaRepository<BlackListToken, String> {

    @Query("""
    select  case when count(b) > 0 then true else false end
    from BlackListToken  b where b.token = :token""")
    boolean existsByToken(String token);

    @Query("""
    delete from BlackListToken  b
    where b.expires_at < :time """)
    long deleteByExpiresAtBefore(java.time.LocalDateTime time);
}
