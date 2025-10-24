package com.login.Login.repository;

import com.login.Login.entity.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // Find a role by its name (case-insensitive)
    Optional<Role> findByNameIgnoreCase(String name);

    @Query("SELECT r FROM Role r WHERE LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Role> searchRoles(@Param("keyword") String keyword, Pageable pageable);
}

