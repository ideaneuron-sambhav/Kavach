package com.login.Login.repository;

import com.login.Login.entity.Credentials;
import com.login.Login.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CredentialsRepository extends JpaRepository<Credentials, Long> {
    List<Credentials> findAllByClientsId(Long clientsId);

    @Query("SELECT cr FROM Credentials cr " +
            "WHERE LOWER(cr.platformName) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
            "   OR LOWER(cr.clients.email) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Credentials> searchAllCredentials(@Param("keyword") String keyword, Pageable pageable);

    @Query("""
       SELECT cr FROM Credentials cr
       WHERE cr.clients.assignedUser = :user
         AND (LOWER(cr.platformName) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(cr.clients.email) LIKE LOWER(CONCAT('%', :keyword, '%')))
       """)
    Page<Credentials> searchAssignedCredentials(@Param("user") User user,
                                                @Param("keyword") String keyword,
                                                Pageable pageable);
}
