package com.login.Login.repository;

import com.login.Login.entity.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PlatformRepository extends JpaRepository<Platform, Long> {

    // Optional: check if platform name already exists
    boolean existsByName(String name);
}
