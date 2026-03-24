package com.jk.authservice.repository;

import com.jk.authservice.entity.User;
import com.jk.authservice.enums.AccountStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.id = :userId")
    Optional<User> findByIdWithRoles(@Param("userId") Long userId);

    Page<User> findByAccountStatus(AccountStatus accountStatus, Pageable pageable);

    @Query("SELECT u FROM User u WHERE " +
            "(LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) " +
            " OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :search, '%'))) " +
            "AND (:status IS NULL OR u.accountStatus = :status)")
    Page<User> searchUsers(
            @Param("search") String search,
            @Param("status") AccountStatus status,
            Pageable pageable);
}
