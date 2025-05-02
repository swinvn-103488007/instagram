package com.engineerpro.instagram.repository;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.engineerpro.instagram.model.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    @EntityGraph(attributePaths = "authorities")
    Optional<User> findByUsername(String username);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
