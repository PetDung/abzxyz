package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.RecipientAddress;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecipientAddressRepository extends JpaRepository<RecipientAddress, String> {
}
