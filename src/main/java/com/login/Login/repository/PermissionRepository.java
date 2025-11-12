package com.login.Login.repository;

import com.login.Login.entity.Clients;
import com.login.Login.entity.Permission;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PermissionRepository extends JpaRepository<Permission, Long> {
    List<Permission> findAllByIdInAndActiveTrue(Iterable<Long> ids);
    Optional<Permission> findByPermissionType(String permissionType);
    @NotNull List<Permission> findAll();
    @Query("SELECT c FROM Permission c WHERE " +
            "LOWER(c.permissionType) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Permission> searchByPermissionType(@Param("keyword") String keyword, Pageable pageable);
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

    @Query("SELECT p FROM Permission p WHERE LOWER(p.permissionType) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Permission> searchRoles(@Param("keyword") String keyword, Pageable pageable);
}

