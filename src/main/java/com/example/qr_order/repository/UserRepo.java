package com.example.qr_order.repository;

import com.example.qr_order.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    @Query("""
    select u from User u 
    where u.user_name = :user_name
    """)
    Optional<User> findByUserName(String user_name);

    @Query("""
    select case when count(u) > 0 then true else false end
    from User u where u.user_name = :user_name
    """)
    boolean existsByUserName(@Param("user_name") String user_name);
}
