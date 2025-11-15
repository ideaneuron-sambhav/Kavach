package com.login.Login.repository;

import com.login.Login.entity.Clients;
import com.login.Login.entity.Groups;
import com.login.Login.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Clients, Long> {
    boolean existsByEmail(String email);
    boolean existsByAlias(String alias);
    boolean existsByMobileNumber(String mobileNumber);
    Page<Clients> findByGroups(Groups groups, Pageable pageable);
    Optional<Clients> findByEmail(String email);
    @Query("SELECT c FROM Clients c WHERE " +
            "LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Clients> searchByNameOrEmail(@Param("keyword") String keyword, Pageable pageable);
    @Query("""
       SELECT c FROM Clients c
       WHERE c.assignedUser = :user
         AND c.active = true
         AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
       """)
    Page<Clients> searchAssignedClients(@Param("user") User user,
                                        @Param("keyword") String keyword,
                                        Pageable pageable);

    @Query("SELECT c FROM Clients c WHERE c.groups = :groups" +
            " AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Clients> searchByNameOrEmailAndGroup(@Param("groups") Groups groups, @Param("keyword") String keyword, Pageable pageable);

    @Query("""
            SELECT c FROM Clients c
       WHERE c.assignedUser = :user
         AND c.groups = :groups
         AND c.active = true
         AND (LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
       """)
    Page<Clients> searchByAssignedClientsAndGroups(@Param("user") User user,
                                                   @Param("groups") Groups groups,
                                                   @Param("keyword") String keyword,
                                                   Pageable pageable);
}

