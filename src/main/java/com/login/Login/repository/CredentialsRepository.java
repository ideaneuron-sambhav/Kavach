package com.login.Login.repository;

import com.login.Login.entity.Credentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CredentialsRepository extends JpaRepository<Credentials, Long> {
    List<Credentials> findAllByClientsId(Long clientsId);
}
