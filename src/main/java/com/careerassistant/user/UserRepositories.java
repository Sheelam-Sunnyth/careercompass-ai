package com.careerassistant.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

interface UserProfileRepository extends JpaRepository<UserProfile, Long> {
    Optional<UserProfile> findByUserId(Long userId);
}
