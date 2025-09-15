package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.MappingUserSystem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MappingUserSystemRepository extends JpaRepository<MappingUserSystem, String> {
    Optional<MappingUserSystem> findByUsername(String username);
}
