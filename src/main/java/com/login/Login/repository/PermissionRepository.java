package com.login.Login.repository;

import com.login.Login.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    List<Permission> findAllById(Iterable<Long> ids);
    Optional<Permission> findByPermissionType(String permissionType);
    List<Permission> findAll();
    @Query(
            value = "SELECT * FROM permission p " +
                    "WHERE p.id IN (" +
                    "    SELECT unnest(COALESCE(r.permission_ids, '{}')) " +
                    "    FROM role r " +
                    "    WHERE LOWER(r.name) = LOWER(:name)" + // case-insensitive
                    ")",
            nativeQuery = true
    )
    List<Permission> findPermissionsByRoleName(@Param("name") String name);

    // Optional: You can add custom queries here if needed
}

