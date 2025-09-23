package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.Auth.Setting;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettingRepository extends JpaRepository<Setting, String> {

    Optional<Setting> findByAccount_Id(String accountId);

}
