package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.Return.Return;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReturnRepository extends JpaRepository<Return, String> {
}
