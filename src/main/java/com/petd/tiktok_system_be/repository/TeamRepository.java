package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.Team;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, String> {
}
