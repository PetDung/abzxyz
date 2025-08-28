package com.petd.tiktok_system_be.repository;

import com.petd.tiktok_system_be.entity.Setting;

import org.springframework.data.jpa.repository.JpaRepository;
public interface SettingRepository extends JpaRepository<Setting, String> {

}
