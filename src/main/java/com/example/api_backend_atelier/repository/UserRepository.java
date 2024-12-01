package com.example.api_backend_atelier.repository;

import com.example.api_backend_atelier.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<AppUser, UUID> {

    Optional<AppUser> findById(UUID id);
    Optional<AppUser> findByNumber(String number);

}
