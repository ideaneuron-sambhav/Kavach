package com.login.Login.repository;

import com.login.Login.entity.Groups;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface GroupsRepository extends JpaRepository<Groups, Long> {
    Optional<Groups> findAllById(Long id);
    Optional<Groups> findAllByName(String name);
    boolean existsByName(String name);
    boolean existsByAlias(String alias);

    @Query("SELECT g FROM Groups g WHERE " +
            "LOWER(g.name) LIKE LOWER(CONCAT('%', :keyword, '%'))" +
            "OR LOWER(g.representativeName) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Groups> searchByNameOrRepresentativeName(@Param("keyword") String keyword, Pageable pageable);
}
