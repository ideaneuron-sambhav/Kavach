package com.login.Login.repository;

import com.login.Login.entity.Role;
import com.login.Login.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByEmail(String email);
    Optional<User> findByEmail(String email);
    List<User> findAllByRole(Role role);
    Page<User> findAllByRoleNot(Pageable Page, Role role);
    @Query("""
    SELECT u FROM User u
    WHERE u.maskedEmail ILIKE CONCAT('%', :keyword, '%') 
    AND NOT (u.role = :role)
    """)
    Page<User> searchUsers(@Param("keyword") String keyword, Pageable pageable, Role role);
}
