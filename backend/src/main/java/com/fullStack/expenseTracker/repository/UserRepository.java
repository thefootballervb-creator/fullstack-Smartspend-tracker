package com.fullStack.expenseTracker.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.fullStack.expenseTracker.models.User;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    User findByVerificationCode(String verificationCode);

    Boolean existsByUsername(String username);

    Boolean existsByEmail(String email);

    @Query(value = "SELECT * from users u " +
            "JOIN user_roles ur ON u.id = ur.user_id " +
            "WHERE ur.role_id = :roleId AND (u.username LIKE CONCAT('%', :keyword, '%') OR u.email LIKE CONCAT('%', :keyword, '%'))", nativeQuery = true)
    Page<User> findAll(Pageable pageable, @Param("roleId") int roleId, @Param("keyword") String keyword);

}
