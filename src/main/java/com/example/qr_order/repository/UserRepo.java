package com.example.qr_order.repository;

import com.example.qr_order.entity.User;
import com.example.qr_order.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepo extends JpaRepository<User, Long> {
    @Query("""
            select u from User u
            where u.userName = :userName
            """)
    Optional<User> findByUserName(String userName);

    @Query("""
            select case when count(u) > 0 then true else false end
            from User u where u.userName = :userName
            """)
    boolean existsByUserName(@Param("userName") String userName);

    List<User> findByRoleNotOrderByCreatedAtDesc(Role role);
}
