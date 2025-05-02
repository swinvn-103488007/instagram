package com.engineerpro.instagram.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.engineerpro.instagram.model.Profile;

@Repository
public interface ProfileRepository extends JpaRepository<Profile, Integer> {
  Profile findOneByUserId(String userId);
}
